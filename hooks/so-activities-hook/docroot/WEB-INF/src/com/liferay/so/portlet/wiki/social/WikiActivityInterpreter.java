/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.so.portlet.wiki.social;

import com.liferay.compat.portal.service.ServiceContext;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portal.util.PortletKeys;
import com.liferay.portlet.PortletURLFactoryUtil;
import com.liferay.portlet.asset.model.AssetRenderer;
import com.liferay.portlet.social.model.SocialActivity;
import com.liferay.portlet.social.model.SocialActivityConstants;
import com.liferay.portlet.wiki.model.WikiNode;
import com.liferay.portlet.wiki.model.WikiPage;
import com.liferay.portlet.wiki.model.WikiPageResource;
import com.liferay.portlet.wiki.service.WikiNodeLocalServiceUtil;
import com.liferay.portlet.wiki.service.WikiPageResourceLocalServiceUtil;
import com.liferay.so.activities.model.SOBaseSocialActivityInterpreter;

import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;

/**
 * @author Jonathan Lee
 */
public class WikiActivityInterpreter extends SOBaseSocialActivityInterpreter {

	public String[] getClassNames() {
		return _CLASS_NAMES;
	}

	@Override
	protected String getBody(
			SocialActivity activity, ServiceContext serviceContext)
		throws Exception {

		AssetRenderer assetRenderer = getAssetRenderer(activity);

		String linkURL = getLinkURL(activity, serviceContext);

		String pageTitle = wrapLink(
			linkURL,
			HtmlUtil.escape(
				assetRenderer.getTitle(serviceContext.getLocale())));

		LiferayPortletRequest liferayPortletRequest =
			serviceContext.getLiferayPortletRequest();

		if (Validator.isNotNull(
				assetRenderer.getIconPath(liferayPortletRequest))) {

			pageTitle = wrapLink(
				linkURL, assetRenderer.getIconPath(liferayPortletRequest),
				HtmlUtil.escape(
					assetRenderer.getTitle(serviceContext.getLocale())));
		}

		StringBundler sb = new StringBundler(5);

		sb.append("<div class=\"activity-body\"><div class=\"title\">");
		sb.append(pageTitle);
		sb.append("</div><div class='wiki-page-content'>");
		sb.append(
			StringUtil.shorten(
				assetRenderer.getSummary(serviceContext.getLocale()), 200));
		sb.append("</div></div>");

		return sb.toString();
	}

	@Override
	protected String getLink(
			SocialActivity activity, ServiceContext serviceContext)
		throws Exception {

		return wrapLink(
			getLinkURL(activity, serviceContext),
			serviceContext.translate("view-wiki"));
	}

	protected Object[] getTitleArguments(
			String groupName, SocialActivity activity, String link,
			String title, ServiceContext serviceContext)
		throws Exception {

		WikiPageResource pageResource =
			WikiPageResourceLocalServiceUtil.getPageResource(
				activity.getClassPK());

		WikiNode node = WikiNodeLocalServiceUtil.getNode(
			pageResource.getNodeId());

		if (node == null) {
			return null;
		}

		String nodeTitle = null;

		long plid = PortalUtil.getPlidFromPortletId(
			activity.getGroupId(), false, PortletKeys.WIKI);

		if (plid > 0) {
			PortletURL nodeURL = PortletURLFactoryUtil.create(
				serviceContext.getLiferayPortletRequest(), PortletKeys.WIKI,
				plid, PortletRequest.RENDER_PHASE);

			nodeURL.setParameter("struts_action", "/wiki/view");
			nodeURL.setParameter("nodeId", String.valueOf(node.getNodeId()));

			nodeTitle = wrapLink(
				nodeURL.toString(), HtmlUtil.escape(node.getName()));
		}
		else {
			nodeTitle = HtmlUtil.escape(node.getName());
		}

		return new Object[] {nodeTitle};
	}

	protected String getTitlePattern(String groupName, SocialActivity activity)
		throws Exception {

		String titlePattern = null;

		if ((activity.getType() == _ADD_COMMENT) ||
			(activity.getType() == SocialActivityConstants.TYPE_ADD_COMMENT)) {

			titlePattern = "commented-on-a-wiki-page";
		}
		else if (activity.getType() == _ADD_PAGE) {
			titlePattern = "created-a-new-wiki-page";
		}
		else if (activity.getType() == _UPDATE_PAGE) {
			titlePattern = "updated-a-wiki-page";
		}
		else {
			return StringPool.BLANK;
		}

		WikiPageResource pageResource =
			WikiPageResourceLocalServiceUtil.getPageResource(
				activity.getClassPK());

		WikiNode node = WikiNodeLocalServiceUtil.getNode(
			pageResource.getNodeId());

		if (Validator.isNotNull(node)) {
			titlePattern = titlePattern.concat("-in-the-x-wiki");
		}

		return titlePattern;
	}

	private static final int _ADD_COMMENT = 3;

	private static final int _ADD_PAGE = 1;

	private static final String[] _CLASS_NAMES = new String[] {
		WikiPage.class.getName()
	};

	private static final int _UPDATE_PAGE = 2;

}