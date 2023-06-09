/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.onlinesectioning.specreg;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CheckCoursesResponse;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.CourseMessage;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.onlinesectioning.custom.WaitListValidationProvider;
import org.unitime.timetable.onlinesectioning.model.XStudent;

/**
 * @author Tomas Muller
 */
public class WaitListCheckValidation implements OnlineSectioningAction<CheckCoursesResponse>{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private CourseRequestInterface iRequest = null;
	private boolean iSubmitIfNoConfims = false;
	
	public WaitListCheckValidation withRequest(CourseRequestInterface request) {
		iRequest = request;
		return this;
	}
	
	public WaitListCheckValidation withSubmitIfNoConfirms(boolean submitIfNoConfims) {
		iSubmitIfNoConfims = submitIfNoConfims;
		return this;
	}
	
	public Long getStudentId() { return iRequest.getStudentId(); }

	@Override
	public CheckCoursesResponse execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		if (!server.getAcademicSession().isSectioningEnabled() || !Customization.WaitListValidationProvider.hasProvider())
			throw new SectioningException(MSG.exceptionNotSupportedFeature());

		CheckCoursesResponse response = new CheckCoursesResponse();

		Lock lock = server.lockStudent(getStudentId(), null, name());
		try {
			OnlineSectioningLog.Action.Builder action = helper.getAction();
						
			XStudent student = server.getStudent(getStudentId());

			action.getStudentBuilder().setUniqueId(student.getStudentId())
				.setExternalId(student.getExternalId())
				.setName(student.getName());
			
			WaitListValidationProvider provider = Customization.WaitListValidationProvider.getProvider();
			
			provider.validate(server, helper, iRequest, response);
			
			if (response.hasMessages())
				for (CourseMessage m: response.getMessages())
					if (m.hasCourse())
						action.addMessageBuilder().setText(m.toString()).setLevel(m.isError() ? OnlineSectioningLog.Message.Level.ERROR : m.isConfirm() ? OnlineSectioningLog.Message.Level.WARN : OnlineSectioningLog.Message.Level.INFO);
			if (response.hasCreditNote())
				action.addMessageBuilder().setText(response.getCreditNote()).setLevel(OnlineSectioningLog.Message.Level.INFO);
			if (response.hasCreditWarning())
				action.addMessageBuilder().setText(response.getCreditWarning()).setLevel(OnlineSectioningLog.Message.Level.WARN);
			if (response.hasErrorMessage())
				action.addMessageBuilder().setText(response.getErrorMessage()).setLevel(OnlineSectioningLog.Message.Level.ERROR);
			
			if (response.isError())
				action.setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
			else if (response.isConfirm())
				action.setResult(OnlineSectioningLog.Action.ResultType.FALSE);
			else
				action.setResult(OnlineSectioningLog.Action.ResultType.TRUE);
		} finally {
			lock.release();
		}
		
		if (iSubmitIfNoConfims && !response.isConfirm()) {
			iRequest.addConfirmations(response.getMessages());
			new WaitListSubmitOverrides().withRequest(iRequest).execute(server, helper);
		}
		
		return response;
	}
	
	@Override
	public String name() {
		return "wait-validate";
	}
}
