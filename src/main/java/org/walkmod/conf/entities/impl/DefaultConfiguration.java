/* 
  Copyright (C) 2013 Raquel Pau and Albert Coroleu.
 
 Walkmod is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Walkmod is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/
package org.walkmod.conf.entities.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.BeanFactory;
import org.walkmod.conf.entities.ChainConfig;
import org.walkmod.conf.entities.Configuration;
import org.walkmod.conf.entities.MergePolicyConfig;
import org.walkmod.conf.entities.PluginConfig;
import org.walkmod.exceptions.WalkModException;
import org.walkmod.merger.MergeEngine;
import org.walkmod.walkers.VisitorMessage;

public class DefaultConfiguration implements Configuration {

	private Map<String, Object> parameters;

	private Map<String, ChainConfig> architectures;

	private BeanFactory beanFactory;
	
	private Collection<PluginConfig> plugins;
	
	private ClassLoader classLoader = null;
	
	private Collection<MergePolicyConfig> mergePolicies;
	
	private Map<String, MergeEngine> mergeEngines;
	
	private String defaultLanguage;

	public DefaultConfiguration() {
		this.parameters = new HashMap<String, Object>();
		this.architectures = new HashMap<String, ChainConfig>();
		this.mergeEngines = new HashMap<String, MergeEngine>();
		this.beanFactory = null;
		this.defaultLanguage = "java";
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public Collection<ChainConfig> getChainConfigs() {
		return architectures.values();
	}

	public void setTransformationChainConfigs(
			Collection<ChainConfig> architectures) {
		this.architectures.clear();
		Iterator<ChainConfig> it = architectures.iterator();
		while (it.hasNext()) {
			ChainConfig current = it.next();
			current.setConfiguration(this);
			this.architectures.put(current.getName(), current);
		}
	}

	public boolean addTransformationChainConfig(ChainConfig architecture) {
		boolean result = architectures.containsKey(architecture.getName());
		if (!result) {
			architecture.setConfiguration(this);
			architectures.put(architecture.getName(), architecture);
		}
		return result;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	@Override
	public Object getBean(String name, Map<?, ?> parameters) {
		Object result = null;
		if (beanFactory != null && beanFactory.containsBean(name)) {
			result = beanFactory.getBean(name);
		}
		if (result == null) {
			try {
				Class<?> clazz = getClassLoader().loadClass(name);
				result = clazz.newInstance();
			} catch (Exception e) {
				throw new WalkModException(
						"Sorry, it is impossible to load the bean "
								+ name
								+ ". Please, assure that it is a valid class name and the library which contains it is in the classpath",
						e);
			}
		}
		if (result != null) {
			BeanWrapper bw = new BeanWrapperImpl(result);
			bw.setPropertyValues(parameters);
		}
		return result;
	}
	
	public void populate(Object element, Map<?, ?> parameters){
		if (element != null) {
			BeanWrapper bw = new BeanWrapperImpl(element);
			bw.setPropertyValues(parameters);
		}
	}

	public Collection<VisitorMessage> getVisitorMessages() {
		Collection<VisitorMessage> result = new LinkedList<VisitorMessage>();
		if (getChainConfigs() != null) {
			for (ChainConfig aqConfig : getChainConfigs()) {
				result.addAll(aqConfig.getWalkerConfig().getWalker()
						.getVisitorMessages());
			}
		}
		return result;
	}

	@Override
	public Collection<PluginConfig> getPlugins() {

		return plugins;
	}

	@Override
	public void setPlugins(Collection<PluginConfig> plugins) {
		this.plugins = plugins;
	}

	@Override
	public ClassLoader getClassLoader() {
		if(classLoader == null){
			return getClass().getClassLoader();
		}
		return classLoader;
	}

	@Override
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	public Collection<MergePolicyConfig> getMergePolicies() {
		return mergePolicies;
	}

	@Override
	public void setMergePolicies(Collection<MergePolicyConfig> mergePolicies) {
		this.mergePolicies = mergePolicies;
	}

	@Override
	public void setMergeEngines(Map<String, MergeEngine> mergeEngines) {
		this.mergeEngines = mergeEngines;
	}

	@Override
	public MergeEngine getMergeEngine(String name) {			
		return mergeEngines.get(name);
	}

	@Override
	public String getDefaultLanguage() {
		
		return defaultLanguage;
	}

	@Override
	public void setDefaultLanguage(String defaults) {
		this.defaultLanguage = defaults;
	}
}
