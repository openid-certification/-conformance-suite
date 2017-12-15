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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.SecureRandom;

import org.bouncycastle.crypto.tls.AlertDescription;
import org.bouncycastle.crypto.tls.Certificate;
import org.bouncycastle.crypto.tls.CertificateRequest;
import org.bouncycastle.crypto.tls.DefaultTlsClient;
import org.bouncycastle.crypto.tls.ProtocolVersion;
import org.bouncycastle.crypto.tls.TlsAuthentication;
import org.bouncycastle.crypto.tls.TlsClient;
import org.bouncycastle.crypto.tls.TlsClientProtocol;
import org.bouncycastle.crypto.tls.TlsCredentials;
import org.bouncycastle.crypto.tls.TlsFatalAlertReceived;

import com.google.common.base.Strings;

import io.fintechlabs.testframework.logging.EventLog;
import io.fintechlabs.testframework.testmodule.Environment;

/**
 * @author jricher
 *
 */
public class DisallowTLS10 extends AbstractCondition {

	// Signals that the connection was aborted after discovering the server version
	@SuppressWarnings("serial")
	private static class ServerHelloReceived extends IOException {

		private ProtocolVersion serverVersion;

		public ServerHelloReceived(ProtocolVersion serverVersion) {
			this.serverVersion = serverVersion;
		}

		public ProtocolVersion getServerVersion() {
			return serverVersion;
		}

	}
	
	/**
	 * @param testId
	 * @param log
	 */
	public DisallowTLS10(String testId, EventLog log, boolean optional) {
		super(testId, log, optional, "FAPI-1-7.1-1");
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see io.fintechlabs.testframework.condition.Condition#evaluate(io.fintechlabs.testframework.testmodule.Environment)
	 */
	@Override
	@PreEnvironment(required = "config")
	public Environment evaluate(Environment env) {

		String tlsTestHost = env.getString("config", "tls.testHost");
		Integer tlsTestPort = env.getInteger("config", "tls.testPort");

		if (Strings.isNullOrEmpty(tlsTestHost)) {
			return error("Couldn't find host to connect for TLS");
		}

		if (tlsTestPort == null) {
			return error("Couldn't find port to connect for TLS");
		}

		try {
			Socket socket = new Socket(InetAddress.getByName(tlsTestHost), tlsTestPort);

			try {

				TlsClientProtocol protocol = new TlsClientProtocol(socket.getInputStream(), socket.getOutputStream(), new SecureRandom());

				TlsClient client = new DefaultTlsClient() {

					@Override
					public TlsAuthentication getAuthentication() {
						return new TlsAuthentication() {

							@Override
							public TlsCredentials getClientCredentials(CertificateRequest certificateRequest) throws IOException {
								return null;
							}

							@Override
							public void notifyServerCertificate(Certificate serverCertificate) throws IOException {
								// even though we make a TLS connection we ignore the server cert validation here
							}
						};
					}

					@Override
					public ProtocolVersion getMinimumVersion() {
						// Disallow anything earlier than TLS 1.0
						return ProtocolVersion.TLSv10;
					}

					@Override
					public ProtocolVersion getClientVersion() {
						// Try to connect with TLS 1.0
						return ProtocolVersion.TLSv10;
					}

					@Override
					public void notifyServerVersion(ProtocolVersion serverVersion) throws IOException {
						// don't need to proceed further
						throw new ServerHelloReceived(serverVersion);
					}
				};

				protocol.connect(client);

				// By the time handshake completes an exception should have been thrown, but just in case:
				return error("Connection completed unexpectedly");

			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					// Don't care
				}
			}
		} catch (ServerHelloReceived e) {
			ProtocolVersion serverVersion = e.getServerVersion();
			if (serverVersion == ProtocolVersion.TLSv10) {
				return error("Server agreed to disallowed TLS 1.0", args("host", tlsTestHost, "port", tlsTestPort));
			} else {
				return error("Server used incorrect TLS version",
						args("server_version", serverVersion.toString(),
								"host", tlsTestHost,
								"port", tlsTestPort));
			}
		} catch (IOException e) {
			if ((e instanceof TlsFatalAlertReceived)
					&& ((TlsFatalAlertReceived) e).getAlertDescription() == AlertDescription.handshake_failure) {
				// If we get here then we haven't received a server hello agreeing on a version
				logSuccess("Server refused TLS 1.0 handshake", args("host", tlsTestHost, "port", tlsTestPort));
			    return env;
			} else {
				return error("Failed to make TLS connection", e, args("host", tlsTestHost, "port", tlsTestPort));
			}
		}
		
	}

}
