package br.com.ndevfactory.persistence;

import java.util.ResourceBundle;

public class PersistenceConfigBuilder {
	
	private PersistenceConfig instance;
	
	private void createAndLoadPersistenceConfig(ResourceBundle resource) {
		this.instance = new PersistenceConfig();
		this.instance.setPersistenceUnitName(resource.getString(PersistenceConsts.BUNDLE_KEY_PERSISTENCE_UNIT_NAME));
	}
	
	public PersistenceConfigBuilder fromBundle() {
		ResourceBundle resource = ResourceBundle.getBundle(PersistenceConsts.BUNDLE_NAME);
		createAndLoadPersistenceConfig(resource);
		return this;
	}

	public PersistenceConfig build() {
		return instance;
	}

}
