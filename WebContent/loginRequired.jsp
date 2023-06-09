<!DOCTYPE html>
<%--
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
--%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
<loc:bundle name="CourseMessages">
<DIV align="center" class="H1">
	<br><br>
	<s:property value="message" escapeHtml="false"/>
	<br><br>
	<A class="l7" href="javascript:self.history.back();"><loc:message name="linkBACK"/></A>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	<s:if test="target != null && !target.isEmpty()">
		<A class="l7" href="${fn:escapeXml(request.getContextPath())}/login.action?target=${target}" target="_top"><loc:message name="linkLOGIN"/></A>
	</s:if>
	<s:else>
		<A class="l7" href="${fn:escapeXml(request.getContextPath())}/login.action" target="_top"><loc:message name="linkLOGIN"/></A>
	</s:else>
	<BR><BR>
</DIV>
</loc:bundle>
