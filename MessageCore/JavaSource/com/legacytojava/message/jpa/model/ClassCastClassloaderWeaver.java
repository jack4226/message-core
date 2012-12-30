package com.legacytojava.message.jpa.model;

import org.springframework.instrument.classloading.SimpleLoadTimeWeaver;

public class ClassCastClassloaderWeaver extends SimpleLoadTimeWeaver {
	// solves sick problem with MyBean cannot be cast to MyBean
	@Override
	public ClassLoader getInstrumentableClassLoader() {
		return super.getInstrumentableClassLoader().getParent();
	}
}
