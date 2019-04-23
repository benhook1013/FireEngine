package com.github.benhook1013.fireengine.client_io.exceptions;

import java.io.IOException;

/*
 *    Copyright 2017 Ben Hook
 *    ClientConnectionException.java
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    		http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

public class ClientConnectionException extends IOException {
	private static final long serialVersionUID = 1L;

	public ClientConnectionException(String message) {
		super(message);
	}

	public ClientConnectionException(Throwable throwable) {
		super(throwable);
	}

	public ClientConnectionException(String message, Throwable throwable) {
		super(message, throwable);
	}
}