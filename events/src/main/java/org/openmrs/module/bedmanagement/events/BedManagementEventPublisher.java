package org.openmrs.module.bedmanagement.events;

import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.annotation.OpenmrsProfile;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.lang.NonNull;

@OpenmrsProfile(modules = { "eventoutbox:*" })
public class BedManagementEventPublisher implements ApplicationEventPublisherAware {
	
	private ApplicationEventPublisher eventPublisher;
	
	@Override
	public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
		this.eventPublisher = applicationEventPublisher;
	}
	
	public void publishEvent(EMREvent<?> event) {
		this.eventPublisher.publishEvent(event);
	}
}
