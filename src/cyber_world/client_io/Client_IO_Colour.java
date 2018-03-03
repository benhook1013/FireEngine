/*
 * Copyright 2017 Benjamin James Hook
 * Client_IO_Colour.java
 */
package cyber_world.client_io;

/*
 *    Copyright 2017 Ben Hook
 *    Client_IO_Colour.java
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

// Mock-static class.
public final class Client_IO_Colour {
	public static enum COLOURS {
		BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE, BRIGHTBLACK, BRIGHTRED, BRIGHTGREEN, BRIGHTYELLOW, BRIGHTBLUE, BRIGHTMAGENTA, BRIGHTCYAN, BRIGHTWHITE
	}

	private Client_IO_Colour() {
	}

	public static ClientConnectionOutput showColours() {
		ClientConnectionOutput output = new ClientConnectionOutput(26);
		output.addPart("Foregrounds:", Client_IO_Colour.COLOURS.WHITE, null);
		output.newLine();
		output.addPart("BLACKBLACKBLACKBLACK", Client_IO_Colour.COLOURS.BLACK, Client_IO_Colour.COLOURS.WHITE);
		output.newLine();
		output.addPart("REDREDREDREDREDREDRE", Client_IO_Colour.COLOURS.RED, null);
		output.newLine();
		output.addPart("GREENGREENGREENGREEN", Client_IO_Colour.COLOURS.GREEN, null);
		output.newLine();
		output.addPart("YELLOWYELLOWYELLOWYE", Client_IO_Colour.COLOURS.YELLOW, null);
		output.newLine();
		output.addPart("BLUEBLUEBLUEBLUEBLUE", Client_IO_Colour.COLOURS.BLUE, null);
		output.newLine();
		output.addPart("MAGENTAMAGENTAMAGENT", Client_IO_Colour.COLOURS.MAGENTA, null);
		output.newLine();
		output.addPart("CYANCYANCYANCYANCYAN", Client_IO_Colour.COLOURS.CYAN, null);
		output.newLine();
		output.addPart("WHITEWHITEWHITEWHITE", Client_IO_Colour.COLOURS.WHITE, null);
		output.newLine();
		output.addPart("BRIGHTBLACKBRIGHTBLA", Client_IO_Colour.COLOURS.BRIGHTBLACK, null);
		output.newLine();
		output.addPart("BRIGHTREDBRIGHTREDBR", Client_IO_Colour.COLOURS.BRIGHTRED, null);
		output.newLine();
		output.addPart("BRIGHTGREENBRIGHTGRE", Client_IO_Colour.COLOURS.BRIGHTGREEN, null);
		output.newLine();
		output.addPart("BRIGHTYELLOWBRIGHTYE", Client_IO_Colour.COLOURS.BRIGHTYELLOW, null);
		output.newLine();
		output.addPart("BRIGHTBLUEBRIGHTBLUE", Client_IO_Colour.COLOURS.BRIGHTBLUE, null);
		output.newLine();
		output.addPart("BRIGHTMAGENTABRIGHTM", Client_IO_Colour.COLOURS.BRIGHTMAGENTA, null);
		output.newLine();
		output.addPart("BRIGHTCYANBRIGHTCYAN", Client_IO_Colour.COLOURS.BRIGHTCYAN, null);
		output.newLine();
		output.addPart("BRIGHTWHITEBRIGHTWHI", Client_IO_Colour.COLOURS.BRIGHTWHITE, null);
		output.newLine();
		output.addPart("Backgrounds:", Client_IO_Colour.COLOURS.WHITE, null);
		output.newLine();
		output.addPart("BLACKBLACKBLACKBLACK", Client_IO_Colour.COLOURS.WHITE, Client_IO_Colour.COLOURS.BLACK);
		output.newLine();
		output.addPart("REDREDREDREDREDREDRE", Client_IO_Colour.COLOURS.BLACK, Client_IO_Colour.COLOURS.RED);
		output.newLine();
		output.addPart("GREENGREENGREENGREEN", Client_IO_Colour.COLOURS.BLACK, Client_IO_Colour.COLOURS.GREEN);
		output.newLine();
		output.addPart("YELLOWYELLOWYELLOWYE", Client_IO_Colour.COLOURS.BLACK, Client_IO_Colour.COLOURS.YELLOW);
		output.newLine();
		output.addPart("BLUEBLUEBLUEBLUEBLUE", Client_IO_Colour.COLOURS.BLACK, Client_IO_Colour.COLOURS.BLUE);
		output.newLine();
		output.addPart("MAGENTAMAGENTAMAGENT", Client_IO_Colour.COLOURS.BLACK, Client_IO_Colour.COLOURS.MAGENTA);
		output.newLine();
		output.addPart("CYANCYANCYANCYANCYAN", Client_IO_Colour.COLOURS.BLACK, Client_IO_Colour.COLOURS.CYAN);
		output.newLine();
		output.addPart("WHITEWHITEWHITEWHITE", Client_IO_Colour.COLOURS.BLACK, Client_IO_Colour.COLOURS.WHITE);
		return output;
	}
}
