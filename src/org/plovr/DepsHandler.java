package org.plovr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.plovr.io.Responses;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.javascript.jscomp.CompilerInput;
import com.google.javascript.jscomp.JSModule;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.deps.SortedDependencies;
import com.google.template.soy.SoyFileSet;
import com.sun.net.httpserver.HttpExchange;

final class DepsHandler extends AbstractGetHandler {

	public DepsHandler(CompilationServer server) {
		super(server);
	}

	@Override
	protected void doGet(HttpExchange exchange, QueryData data, Config config)
			throws IOException {
		Compilation compilation;
		
		try {
			compilation = config.getManifest().getCompilerArguments(config.getModuleConfig());
			compilation.compile(config);
		} catch (CompilationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		server.recordCompilation(config, compilation);
		Result result = compilation.getResult();		
		
		Collection<CompilerInput> inputs = compilation.getCompiler().getInputsById().values();
		
		Gson gson = new Gson();
		HashMap<String, HashMap<String, Collection<String>>> map = new HashMap<String, HashMap<String, Collection<String>>>();
		
		for(CompilerInput input : inputs) {
			HashMap<String, Collection<String>> entry = new HashMap<String, Collection<String>>();
			
			entry.put("provides", input.getProvides());
			entry.put("requires", input.getRequires());
			
			map.put(input.getName(), entry);
		}
		
		if (result.success) {
			Responses.writeGson(gson.toJson(map), exchange);
		}
	}

}
