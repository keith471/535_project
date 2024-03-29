package socs.network.util;

import java.io.File;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

public class Configuration {

	private Config _config = null;

	public Configuration(String path) {
		// create a Config file by parsing the config file
		_config = ConfigFactory.parseFile(new File(path));
	}

	public String getString(String key) {
		return _config.getString(key);
	}

	public Boolean getBoolean(String key) {
		return _config.getBoolean(key);
	}

	public int getInt(String key) {
		return _config.getInt(key);
	}

	public short getShort(String key) {
		return (short) _config.getInt(key);
	}

	public double getDouble(String key) {
		return _config.getDouble(key);
	}

	public void addEntry(String key, String value) {
		_config = _config.withValue(key, ConfigValueFactory.fromAnyRef(value));
	}
}
