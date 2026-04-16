package org.openmrs.module.bedmanagement.events;

import org.apache.commons.lang.StringUtils;
import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.api.context.Context;
import org.openmrs.module.bedmanagement.entity.BedTagMap;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

public class BedTagMapAdvice implements AfterReturningAdvice {
	
	static final String BED_TAG_MAP_EVENT_RECORD_GLOBAL_PROPERTY = "eventoutbox.publish.eventsForBedTagMapChange";
	
	static final String BED_TAG_MAP_EVENT_URL_PATTERN_GLOBAL_PROPERTY = "eventoutbox.event.urlPatternForBedTagMap";
	
	private static final String DEFAULT_BED_TAG_MAP_EVENT_URL_PATTERN = "/openmrs/ws/rest/v1/bedTagMap/{uuid}";
	
	public static final String CATEGORY = "bedtagmap";
	
	public static final String TITLE = "Bed-Tag-Map";
	
	private static final String SAVE_METHOD = "save";
	
	private static final String DELETE_METHOD = "delete";
	
	private static final int BED_TAG_MAP_OBJECT_INDEX = 0;
	
	private static final String UUID_PATTERN_TO_REPLACE = "{uuid}";
	
	private final BedManagementEventPublisher eventPublisher;
	
	public BedTagMapAdvice(BedManagementEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}
	
	@Override
	public void afterReturning(Object returnValue, Method method, Object[] parameters, Object o1) {
		if (!shouldRaiseRelationshipEvent()) {
			return;
		}
		String execMethodName = method.getName();
		if (!SAVE_METHOD.equals(execMethodName) && !DELETE_METHOD.equals(execMethodName)) {
			return;
		}
		raiseBedTagMapEvent(returnValue, parameters);
	}
	
	private void raiseBedTagMapEvent(Object returnValue, Object[] parameters) {
		BedTagMap bedTagMap = returnValue == null ? (BedTagMap) parameters[BED_TAG_MAP_OBJECT_INDEX]
		        : (BedTagMap) returnValue;
		String url = getUrlPattern().replace(UUID_PATTERN_TO_REPLACE, bedTagMap.getUuid());
		eventPublisher.publishEvent(new EMREvent<>(bedTagMap, CATEGORY, TITLE, null, url));
	}
	
	boolean shouldRaiseRelationshipEvent() {
		String raiseEvent = Context.getAdministrationService().getGlobalProperty(BED_TAG_MAP_EVENT_RECORD_GLOBAL_PROPERTY);
		return StringUtils.isEmpty(raiseEvent) || Boolean.valueOf(raiseEvent);
	}
	
	private String getUrlPattern() {
		return Context.getAdministrationService().getGlobalProperty(BED_TAG_MAP_EVENT_URL_PATTERN_GLOBAL_PROPERTY,
		    DEFAULT_BED_TAG_MAP_EVENT_URL_PATTERN);
	}
}
