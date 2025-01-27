import threading
import queue
import fnmatch
import logging

from workers.taskqueue import TestTask

logger = logging.getLogger('resultqueue')

class ResultQueue:

    def __init__(self, expected_failures_list):
        self._all_results = {}
        self._expected_failures_list = expected_failures_list
        self._tests_to_retry = []
        self._lock = threading.Lock()

        # created an unbounded queue as the rate of incoming requests is lower than expected summarization proc
        self._queue = queue.Queue()
        consumer = threading.Thread(target=self.consumer)
        consumer.daemon = True
        consumer.start()

    def add_task_result(self, task: TestTask, result):
        self._queue.put((task, result))

    def get_task_results(self):
        self._all_results["tests_to_retry"] = self._tests_to_retry

        return self._all_results

    # consume work
    def consumer(self):
        print('Consumer: Running')
        # consume work
        while True:
            # get a unit of work
            key = self._queue.get()
            # check for stop
            if key is None:
                break

            task = key[0]
            result = key[1]
            succeded = {}
            expected_failed = {}
            failed = {}
            src = task.config["src"]
            all_passed = True
            failed_tests_links = []
            for test, results in result.items():
                if test == 'variant' or test == "url":
                    continue
                test_info = results['info'] if 'info' in results else {}
                test_result = test_info['result'] if 'result' in test_info else "FAILED"
                all_passed = all_passed and test_result == "PASSED"
                if test_result == "PASSED" or test_result == "WARNING" or test_result == "REVIEW":
                    succeded[test] = test_result
                else:
                    if self.expected_failure(self._expected_failures_list, test_info['testName'],  test_info["variant"], task.plan_config_filename, results):
                        expected_failed[test] = test_result
                    else:
                        failed[test] = test_result
                        failed_tests_links.append(results['url'])
                if 'op' in results:
                    op_tests = results['op']
                    op_variant = op_tests['variant']
                    op_config = op_tests['config']
                    op_modules = op_tests['tests']
                    for op_test_name, op_module in op_modules.items():

                        op_test_info = op_module['info'] if 'info' in op_module else {}
                        op_test_result = op_test_info['result'] if 'result' in op_test_info else "FAILED"
                        if op_test_result == "PASSED" or op_test_result == "REVIEW":
                            succeded[op_test_name] = op_test_info
                        else:
                            if self.expected_failure(self._expected_failures_list, op_test_info['testName'],  op_variant, op_config, op_module):
                                expected_failed[op_test_name] = op_test_info
                            else:
                                failed[op_test_name] = op_test_info
                                failed_tests_links.append(f"{results['url']} - failed OP: {op_module['url']} ")

            if failed:
                self._tests_to_retry.append(task.config["src"])

            self._all_results[src] = {
                "succeeded": len(succeded),
                "failed": len(failed),
                "expected_failed": len(expected_failed),
                "failed_tests_links": failed_tests_links,
            }
            self._queue.task_done()
        # all done
        print('Consumer: Done')

    def expected_failure(self, expected_failures, testname, variant, configuration_filename, test_result):
        for expected_failure in expected_failures:
            if expected_failure["test-name"] == testname and fnmatch.fnmatch(configuration_filename, expected_failure['configuration-filename']):

                if not expected_failure["variant"] == "*":
                    # if the expected failure variant accept anything, we don't need to compare the test variants
                    for key, val in expected_failure["variant"].items():
                        if key not in variant:
                            # the key declared
                            continue
                        if not variant[key] == val:
                            continue

                log = test_result["logs"]

                # Filter the starting of blocks and entries with a result
                failed_entries = [x for x in log if 'startBlock' in x or
                                  ('result' in x and not (x['result'] == 'INFO' or x['result'] == 'SUCCESS'))]
                currentBlock = ""
                for line in failed_entries:
                    if 'startBlock' in line:
                        currentBlock = line['msg']
                    elif currentBlock == expected_failure['current-block']:
                        if 'result' in line and 'src' in line and line['src'] == expected_failure['condition']:
                            # the condition and the block were founded
                            if ((expected_failure['expected-result'] == 'warning' and line['result'] == "WARNING") or
                                    (expected_failure['expected-result'] == 'failure' and line['result'] == "FAILURE")):
                                return True
        return False
