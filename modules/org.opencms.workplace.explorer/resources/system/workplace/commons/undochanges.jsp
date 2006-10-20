<%@ page import="
    org.opencms.workplace.CmsDialog,
	org.opencms.workplace.commons.CmsUndoChanges
" %><%	

	// initialize the workplace class
	CmsUndoChanges wp = new CmsUndoChanges(pageContext, request, response);
	
//////////////////// start of switch statement 


switch (wp.getAction()) {

case CmsDialog.ACTION_CANCEL:
//////////////////// ACTION: cancel button pressed

	wp.actionCloseDialog();

break;


case CmsUndoChanges.ACTION_UNDOCHANGES:
case CmsDialog.ACTION_WAIT:

//////////////////// ACTION: main undo changes action with optional wait screen)

	wp.actionUndoChanges();

break;


case CmsDialog.ACTION_LOCKS_CONFIRMED:
//////////////////// ACTION: show undo changes dialog (default)

	wp.setParamAction("undochanges");

%><%= wp.htmlStart("help.explorer.contextmenu.undo_changes") %>
<%= wp.bodyStart("dialog") %>
<%= wp.dialogStart() %>
<%= wp.dialogContentStart(wp.getParamTitle()) %><%
if (wp.isMultiOperation()) { //%>
	<%@ include file="includes/multiresourcelist.txt" %><%
} else { //%>
	<%@ include file="includes/resourceinfo.txt" %><%
} %>

<form name="main" action="<%= wp.getDialogUri() %>" method="post" class="nomargin" onsubmit="return submitAction('<%= CmsDialog.DIALOG_OK %>', null, 'main');">
<%= wp.paramsAsHidden() %>
<input type="hidden" name="<%= CmsDialog.PARAM_FRAMENAME %>" value="">

<%= wp.buildDialogOptions() %>

<%= wp.dialogContentEnd() %>

<%= wp.dialogButtonsOkCancel() %>

</form>

<%= wp.dialogEnd() %>
<%= wp.bodyEnd() %>
<%= wp.htmlEnd() %>
<% 
   break;

case CmsDialog.ACTION_DEFAULT:
default:
%>
<%= wp.buildLockDialog() %>
<% } 
//////////////////// end of switch statement 
%>