/*******************************************************************************
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package io.fintechlabs.testframework.condition;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;

import io.fintechlabs.testframework.logging.EventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author jricher
 *
 */
public class AddNonceToAuthorizationEndpointRequest extends AbstractCondition {

	/**
	 * @param testId
	 * @param log
	 * @param optional
	 */
	public AddNonceToAuthorizationEndpointRequest(String testId, EventLog log, boolean optional) {
		super(testId, log, optional);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	public Environment evaluate(Environment env) {
		String nonce = env.getString("nonce");
		if (Strings.isNullOrEmpty(nonce)) {
			return error("Couldn't find nonce value");
		}
		
		if (!env.containsObj("authorization_endpoint_request")) {
			return error("Couldn't find authorization endpoint request");
		}
		
		JsonObject authorizationEndpointRequest = env.get("authorization_endpoint_request");
		
		authorizationEndpointRequest.addProperty("nonce", nonce);
		
		env.put("authorization_endpoint_request", authorizationEndpointRequest);
		
		logSuccess("Added nonce parameter to request", authorizationEndpointRequest);
		
		return env;

	}

}
