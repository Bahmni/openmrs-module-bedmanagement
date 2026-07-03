package org.openmrs.module.bedmanagement.events;

import org.bahmni.module.eventoutbox.EMREvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.module.bedmanagement.BedDetails;
import org.openmrs.module.bedmanagement.entity.BedPatientAssignment;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BedAssignmentAdviceTest {
	
	private BedAssignmentAdvice advice;
	
	@Mock
	private BedManagementEventPublisher eventPublisher;
	
	@Mock
	private BedDetails bedDetails;
	
	@Mock
	private BedPatientAssignment currentAssignment;
	
	@Mock
	private BedPatientAssignment lastAssignment;
	
	@BeforeEach
	public void setUp() {
		advice = new BedAssignmentAdvice(eventPublisher);
		lenient().when(currentAssignment.getUuid()).thenReturn("current-uuid");
		lenient().when(lastAssignment.getUuid()).thenReturn("last-uuid");
	}
	
	@Test
	public void shouldPublishTwoEventsOnAssignWhenLastAssignmentExists() throws Exception {
		when(bedDetails.getLastAssignment()).thenReturn(lastAssignment);
		when(bedDetails.getCurrentAssignments()).thenReturn(Collections.singletonList(currentAssignment));
		
		advice.afterReturning(bedDetails, getMethod("assignPatientToBed"), null, null);
		
		verify(eventPublisher, times(2)).publishEvent(any(EMREvent.class));
	}
	
	@Test
	public void shouldPublishOneEventOnAssignWhenNoLastAssignment() throws Exception {
		when(bedDetails.getLastAssignment()).thenReturn(null);
		when(bedDetails.getCurrentAssignments()).thenReturn(Collections.singletonList(currentAssignment));
		
		advice.afterReturning(bedDetails, getMethod("assignPatientToBed"), null, null);
		
		verify(eventPublisher, times(1)).publishEvent(any(EMREvent.class));
	}
	
	@Test
	public void shouldPublishEventOnUnassignWithCorrectCategoryAndTitle() throws Exception {
		when(bedDetails.getLastAssignment()).thenReturn(lastAssignment);
		
		advice.afterReturning(bedDetails, getMethod("unAssignPatientFromBed"), null, null);
		
		ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
		verify(eventPublisher).publishEvent(captor.capture());
		assertEquals(BedAssignmentAdvice.CATEGORY, captor.getValue().getCategory());
		assertEquals(BedAssignmentAdvice.TITLE, captor.getValue().getTitle());
		assertTrue(captor.getValue().getContent().contains("last-uuid"));
	}
	
	@Test
	public void shouldNotPublishEventWhenUnassignReturnsNull() throws Exception {
		advice.afterReturning(null, getMethod("unAssignPatientFromBed"), null, null);
		
		verify(eventPublisher, never()).publishEvent(any());
	}
	
	// The advice only checks method.getName(), so stub methods with matching names
	// work
	private java.lang.reflect.Method getMethod(String name) throws Exception {
		return BedAssignmentAdviceTest.class.getMethod(name);
	}
	
	public BedDetails assignPatientToBed() {
		return null;
	}
	
	public BedDetails unAssignPatientFromBed() {
		return null;
	}
}
