package org.openmrs.module.bedmanagement.events;

import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.module.bedmanagement.BedDetails;
import org.openmrs.module.bedmanagement.entity.BedPatientAssignment;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.List;

public class BedAssignmentAdvice implements AfterReturningAdvice {
	
	private static final String TEMPLATE = "/openmrs/ws/rest/v1/bedPatientAssignment/%s?v=custom:(uuid,startDatetime,endDatetime,bed,patient,encounter:(uuid,encounterDatetime,encounterType:(uuid,name),visit:(uuid,startDatetime,visitType)))";
	
	public static final String CATEGORY = "encounter";
	
	public static final String TITLE = "Bed-Assignment";
	
	private static final String ASSIGN_BED_METHOD = "assignPatientToBed";
	
	private static final String UNASSIGN_BED_METHOD = "unAssignPatientFromBed";
	
	private final BedManagementEventPublisher eventPublisher;
	
	public BedAssignmentAdvice(BedManagementEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}
	
	@Override
	public void afterReturning(Object returnValue, Method method, Object[] args, Object target) {
		String execMethodName = method.getName();
		if (execMethodName.equals(ASSIGN_BED_METHOD)) {
			BedDetails bedDetails = (BedDetails) returnValue;
			BedPatientAssignment lastAssignment = bedDetails.getLastAssignment();
			if (lastAssignment != null) {
				publishEvent(lastAssignment);
			}
			List<BedPatientAssignment> currentAssignments = bedDetails.getCurrentAssignments();
			publishEvent(currentAssignments.get(currentAssignments.size() - 1));
		} else if (execMethodName.equals(UNASSIGN_BED_METHOD)) {
			if (returnValue == null) {
				return;
			}
			publishEvent(((BedDetails) returnValue).getLastAssignment());
		}
	}
	
	private void publishEvent(BedPatientAssignment assignment) {
		if (assignment == null) {
			return;
		}
		String uri = String.format(TEMPLATE, assignment.getUuid());
		eventPublisher.publishEvent(new EMREvent<>(assignment, CATEGORY, TITLE, uri, uri));
	}
}
