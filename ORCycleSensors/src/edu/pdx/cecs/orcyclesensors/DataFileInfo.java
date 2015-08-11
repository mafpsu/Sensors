package edu.pdx.cecs.orcyclesensors;

/**
 * This class designates a file in the data directory.
 * @author Robin Murray
 *
 */
public class DataFileInfo {

	private final String name;
	private final String path;
	private final long length;

	public DataFileInfo(String name, String path, long length) {
		this.name = name;
		this.path = path;
		this.length = length;
	}
	
	/**
	 * Returns the name of the file in the data directory
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the absolute path to the file in the data directory
	 * @return
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * Returns the length of the file in the data directory
	 * @return
	 */
	public long getLength() {
		return length;
	}
}
