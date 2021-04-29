package net.openid.conformance.condition.as.par;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.nimbusds.jose.JOSEException;
import net.openid.conformance.condition.AbstractCondition;
import net.openid.conformance.condition.PostEnvironment;
import net.openid.conformance.condition.PreEnvironment;
import net.openid.conformance.condition.as.AbstractExtractRequestObject;
import net.openid.conformance.testmodule.Environment;
import net.openid.conformance.util.JWTUtil;

import java.text.ParseException;
import java.util.UUID;

public class ExtractRequestParameterFromPAREndpointRequest extends AbstractExtractRequestObject
{

	@Override
	@PreEnvironment(required = {"par_endpoint_http_request"})
	@PostEnvironment(required = "", strings = {"par_endpoint_generated_request_uri"})
	public Environment evaluate(Environment env) {
		String requestParameter = env.getString("par_endpoint_http_request", "body_form_params.request");

		if (Strings.isNullOrEmpty(requestParameter)) {
			throw error("Could not find request parameter in PAR endpoint parameters");
		}

		String requestUri = "urn:ietf:params:oauth:request_uri:" + UUID.randomUUID().toString();
		env.putString("par_endpoint_generated_request_uri", requestUri);

		logSuccess("PAR endpoint request contains a request parameter",
			args("request", requestParameter, "request_uri", requestUri));
		return env;
	}

}
