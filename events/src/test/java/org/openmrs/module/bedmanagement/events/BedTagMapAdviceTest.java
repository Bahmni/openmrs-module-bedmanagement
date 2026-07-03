package org.openmrs.module.bedmanagement.events;

import org.bahmni.module.eventoutbox.EMREvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bedmanagement.entity.BedTagMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BedTagMapAdviceTest {
	
	private BedTagMapAdvice bedTagMapAdvice;
	
	@Mock
	private BedTagMap bedTagMap;
	
	@Mock
	private AdministrationService administrationService;
	
	@Mock
	private BedManagementEventPublisher eventPublisher;
	
	private static final String SOME_UUID = "SOME-UUID";
	
	@BeforeEach
	public void setUp() {
		bedTagMapAdvice = new BedTagMapAdvice(eventPublisher);
		lenient().when(bedTagMap.getUuid()).thenReturn(SOME_UUID);
	}
	
	private MockedStatic<Context> mockContextWithProperty(String enableValue) {
		MockedStatic<Context> ctx = mockStatic(Context.class);
		ctx.when(Context::getAdministrationService).thenReturn(administrationService);
		lenient().when(administrationService.getGlobalProperty(eq(BedTagMapAdvice.BED_TAG_MAP_EVENT_RECORD_GLOBAL_PROPERTY)))
		        .thenReturn(enableValue);
		lenient()
		        .when(administrationService
		                .getGlobalProperty(eq(BedTagMapAdvice.BED_TAG_MAP_EVENT_URL_PATTERN_GLOBAL_PROPERTY), anyString()))
		        .thenReturn("/openmrs/ws/rest/v1/bedTagMap/{uuid}");
		return ctx;
	}
	
	@Test
	public void shouldPublishEventForBedTagMapSave() throws Exception {
		try (MockedStatic<Context> ctx = mockContextWithProperty("true")) {
			bedTagMapAdvice.afterReturning(bedTagMap, this.getClass().getMethod("save"), null, null);
		}
		
		ArgumentCaptor<EMREvent> captor = ArgumentCaptor.forClass(EMREvent.class);
		verify(eventPublisher).publishEvent(captor.capture());
		EMREvent<?> event = captor.getValue();
		assertEquals(BedTagMapAdvice.CATEGORY, event.getCategory());
		assertEquals(BedTagMapAdvice.TITLE, event.getTitle());
		assertEquals("/openmrs/ws/rest/v1/bedTagMap/" + SOME_UUID, event.getContent());
	}
	
	@Test
	public void shouldPublishEventWhenGlobalPropertyIsEmpty() throws Exception {
		try (MockedStatic<Context> ctx = mockContextWithProperty("")) {
			bedTagMapAdvice.afterReturning(bedTagMap, this.getClass().getMethod("save"), null, null);
		}
		verify(eventPublisher).publishEvent(any(EMREvent.class));
	}
	
	@Test
	public void shouldNotPublishEventWhenGlobalPropertyIsFalse() throws Exception {
		try (MockedStatic<Context> ctx = mockContextWithProperty("false")) {
			bedTagMapAdvice.afterReturning(bedTagMap, this.getClass().getMethod("save"), null, null);
		}
		verify(eventPublisher, never()).publishEvent(any());
	}
	
	@Test
	public void shouldNotPublishEventForUninterceptedMethod() throws Exception {
		try (MockedStatic<Context> ctx = mockContextWithProperty("true")) {
			bedTagMapAdvice.afterReturning(bedTagMap, this.getClass().getMethod("someOtherMethod"), null, null);
		}
		verify(eventPublisher, never()).publishEvent(any());
	}
	
	@Test
	public void shouldUseParameterBedTagMapWhenReturnValueIsNull() throws Exception {
		try (MockedStatic<Context> ctx = mockContextWithProperty("true")) {
			Object[] params = { bedTagMap };
			bedTagMapAdvice.afterReturning(null, this.getClass().getMethod("delete"), params, null);
		}
		verify(eventPublisher).publishEvent(any(EMREvent.class));
	}
	
	public void save() {
	}
	
	public void delete() {
	}
	
	public void someOtherMethod() {
	}
}
