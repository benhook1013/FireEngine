package fireengine.character.command.action.exception;

import fireengine.character.command.exception.CommandException;

/*
 *    Copyright 2019 Ben Hook
 *    ActionExceptionNoMatcher.java
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

/**
 * An Exception generated from an Action type Class when no Matcher is supplied.
 * 
 * @author Ben Hook
 */
public class ActionExceptionNoMatcher extends CommandException {
	private static final long serialVersionUID = 1L;

	public ActionExceptionNoMatcher(String message) {
		super(message);
	}

	public ActionExceptionNoMatcher(Throwable throwable) {
		super(throwable);
	}

	public ActionExceptionNoMatcher(String message, Throwable throwable) {
		super(message, throwable);
	}
}
