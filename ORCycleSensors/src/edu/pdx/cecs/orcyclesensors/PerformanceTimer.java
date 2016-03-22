/**
 *  ORcycleSensors, Copyright 2015, PSU Transportation, Technology, and People Lab.
 *
 *  @author Robin Murray <robin5@pdx.edu>    (code)
 *  @author Miguel Figliozzi <figliozzi@pdx.edu> and ORcycle team (general app
 *  design and features, report questionnaires, and features)
 *
 *  For more information on the project, go to
 *  http://www.pdx.edu/transportation-lab/orcycle and http://www.pdx.edu/transportation-lab/app-development
 *
 *  ORcycle is free software: you can redistribute it and/or modify it under the
 *  terms of the GNU General Public License as published by the Free Software
 *  Foundation, either version 3 of the License, or any later version.
 *  ORcycle is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 *  A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  You should have received a copy of the GNU General Public License along with
 *  ORcycle. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package edu.pdx.cecs.orcyclesensors;

import android.util.Log;

/**
 * This class measures elapsed time.
 * @author robin5 (Robin Murray)
 * @version 1.0
 * @see <code>Application<code/> class.
 * created 3/22/2016
 */
public class PerformanceTimer {

	static long startTime = 0;
	static long markTimeIn = 0;
	static long lastTime = 0;
	
	public PerformanceTimer() {
	}
	
	static public void start() {
		lastTime = startTime = System.nanoTime();
	}
	
	static public void mark() {
		markTimeIn = System.nanoTime();
	}
	
	static public void check(String comment) {
		long markTimeOut  = System.nanoTime();
		long elapsed = markTimeOut - markTimeIn;
		Log.v("PT", comment + ": " + elapsed);
	}
}
