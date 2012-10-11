/*-
 * Copyright (C) 2012 ATNoG, Instituto de Telecomunicacoes, Portugal. All rights reserved.
 *
 * This file is part of CIMI-Java.
 *
 * CIMI-Java is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CIMI-Java is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CIMI-Java. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package pt.it.av.atnog.cimi.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;

import pt.it.av.atnog.cimi.model.Collection;
import pt.it.av.atnog.cimi.model.Machine;
import pt.it.av.atnog.cimi.model.MachineCreate;

/**
 * @author <a href="mailto:cgoncalves@av.it.pt">Carlos Gon&ccedil;alves</a>
 */
public class CIMIClient {

	public static String MODEL_NAME_MACHINES = "machines";

	private URL baseUri;
	private Credentials credentials;
	private ApacheHttpClient4Executor executor;
	private DefaultHttpClient httpclient;

	public CIMIClient(URL entryPoint, Credentials credentials) {
		this.baseUri = entryPoint;
		executor = new ApacheHttpClient4Executor();
		httpclient = (DefaultHttpClient) executor.getHttpClient();
		setCredential(credentials);
	}

	public <T> List<T> getModelCollection(Class<T> listOfClass, String uri) throws Exception {
		Collection collection = doRequest(HttpMethod.GET, uri).getEntity(Collection.class);

		List<T> ret = new ArrayList<T>();
		for (Object o : collection.getAnyTargetNamespace()) {
			if (o.getClass().isAssignableFrom(listOfClass)) {
				ret.add((T) o);
			}
		}

		return ret;
	}

	public <T> T getModel(Class<T> returnType, String uri) throws Exception {
		return doRequest(HttpMethod.GET, uri).getEntity(returnType);
	}

	public <T> T postModel(Class<T> returnType, String uri, Object body) throws Exception {
		return doRequest(HttpMethod.POST, uri, body).getEntity(returnType);
	}

	public boolean deleteModel(String uri) throws Exception {
		return doRequest(HttpMethod.DELETE, uri).getStatus() == HttpStatus.SC_OK;
	}

	public <T> T putModel(Class<T> returnType, String uri, Object body) throws Exception {
		return doRequest(HttpMethod.PUT, uri, body).getEntity(returnType);
	}

	public <T> ClientResponse<T> doRequest(String httpMethod, String uri) throws Exception {
		return doRequest(httpMethod, uri, null);
	}

	public <T> ClientResponse<T> doRequest(String httpMethod, String uri, Object body) throws Exception {
		ClientRequest request = new ClientRequest(uri, executor);
		request.header("CIMI-Specification-Version", "1.0");
		request.accept(MediaType.APPLICATION_XML);
		request.setHttpMethod(httpMethod);
		if (body != null) {
			request.body(MediaType.APPLICATION_XML, body);
		}

		ClientResponse<T> response = request.execute();
		return response;
	}

	public void setCredential(Credentials credentials) {
		this.credentials = credentials;
		httpclient.getCredentialsProvider().setCredentials(new AuthScope(baseUri.getHost(), baseUri.getPort()),
		        credentials);
	}

	public void shutdown() {
		httpclient.getConnectionManager().shutdown();
	}

	public String generateUrl(String model) {
		return baseUri + "/" + model;
	}

	public String generateUrl(String model, String id) {
		return generateUrl(model) + "/" + id;
	}

	public String generateUrl(String model, String id, String action) {
		return generateUrl(model, id) + "/" + action;
	}

	/*
	 * CRUD operations
	 */

	public List<Machine> getMachines() throws Exception {
		return getModelCollection(Machine.class, generateUrl(MODEL_NAME_MACHINES));
	}

	public Machine getMachine(String name) throws Exception {
		return getModel(Machine.class, generateUrl(MODEL_NAME_MACHINES, name));
	}

	public Machine createMachine(MachineCreate machineCreate) throws Exception {
		return postModel(Machine.class, generateUrl(MODEL_NAME_MACHINES), machineCreate);
	}

	public Machine updateMachine(Machine machine) throws Exception {
		return putModel(Machine.class, generateUrl(MODEL_NAME_MACHINES, machine.getName()), machine);
	}

	public boolean deleteMachine(String name) throws Exception {
		return deleteModel(generateUrl(MODEL_NAME_MACHINES, name));
	}

}
