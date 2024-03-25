package com.coachingfit.client.mvp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level ;
import java.util.logging.Logger ;

import com.coachingfit.client.global.CoachingFitSupervisor;
import com.coachingfit.client.event.GotoAdminPageEvent;
import com.coachingfit.shared.database.CoachingFitFormData;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.model.CoachingFitUser;
import com.coachingfit.shared.rpc.GetCoachingFitCoachsListAction;
import com.coachingfit.shared.rpc.GetCoachingFitCoachsListResult;
import com.coachingfit.shared.rpc.GetCoachingFitTraineesListAction;
import com.coachingfit.shared.rpc.GetCoachingFitTraineesListResult;
import com.coachingfit.shared.rpc.GetCoachingFormsAction;
import com.coachingfit.shared.rpc.GetCoachingFormsForUserAction;
import com.coachingfit.shared.rpc.GetCoachingFormsForUserResult;
import com.coachingfit.shared.rpc.GetCoachingFormsResult;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import com.primege.client.event.GoToEditFormEvent;
import com.primege.client.event.GoToNewFormEvent;
import com.primege.client.event.GoToOpenDashboardEvent;
import com.primege.client.event.HeaderButtonsEvent;
import com.primege.client.event.LoginSuccessEvent;
import com.primege.client.event.LoginSuccessEventHandler;
import com.primege.client.event.PostLoginHeaderDisplayEvent;
import com.primege.client.global.DataDictionaryCallBack;
import com.primege.client.mvp.PrimegeBaseInterface;
import com.primege.shared.database.ArchetypeData;
import com.primege.shared.database.Dictionary;
import com.primege.shared.database.UserData;
import com.primege.shared.rpc.DeleteFormAction;
import com.primege.shared.rpc.DeleteFormResult;
import com.primege.shared.rpc.GetCsvAction;
import com.primege.shared.rpc.GetCsvResult;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class CoachingFitLoginResponsePresenter extends WidgetPresenter<CoachingFitLoginResponsePresenter.Display>
{
	private final DispatchAsync         _dispatcher ;
	private final CoachingFitSupervisor _supervisor ;
	private       Logger                _logger = Logger.getLogger("") ;

	private       ClickHandler        _newFormClickHandler ;
	private       ClickHandler        _openDashboardClickHandler ;
	private       ClickHandler        _requestCsvClickHandler ;
	private       ClickHandler        _showStaticHtmlClickHandler ;

	private       List<UserData>      _aAllowedCoachs   = new ArrayList<UserData>() ;
	private       List<TraineeData>   _aAllowedTrainees = new ArrayList<TraineeData>() ;

	private       List<Integer>       _aTraineesToFind  = new ArrayList<Integer>() ;
	private       List<Integer>       _aCoachsToFind    = new ArrayList<Integer>() ;

	public interface Display extends PrimegeBaseInterface
	{
		FlowPanel        getWorkspace() ;
		HasClickHandlers getEditUserData() ;
		HasClickHandlers getFormsSearchButton() ;
		HasClickHandlers getEditFormButton() ;
		HasClickHandlers getDeleteFormButton() ;
		HasClickHandlers getAdminPageButton() ;

		void             showAdminPageButton() ;
		void             activateEditFormButton(boolean bActivate) ;
		void             activateDeleteFormButton(boolean bActivate) ;

		void             addNewFormButton(String sCaption, ClickHandler handler, int iArchetypeId) ;
		void             addOpenDashboardButton(String sCaption, ClickHandler handler, int iArchetypeId) ;
		void             addRequestCsvButton(String sCaption, ClickHandler handler, int iArchetypeId) ;
		void             addOpenStaticHtmlButton(String sCaption, ClickHandler handler, int iArchetypeId) ;
		void             showButtons() ;

		int              getArchetypeIdForNewForm(Widget sender) ;
		int              getArchetypeIdForOpenDashboard(Widget sender) ;
		int              getArchetypeIdForRequestCsv(Widget sender) ;
		int              getArchetypeIdForOpenStaticHtml(Widget sender) ;

		void             clearFormsList() ;
		void             addForm(String sFormLabel, int iFormId) ;

		void             setFormDates(Date tDate) ;
		void             setFormDateFrom(Date tDate) ;
		void             setFormDateTo(Date tDate) ;
		String           getFormDateFrom() ;
		String           getFormDateTo() ;

		int              getSelectedCoach() ;
		int              getSelectedCoachingTrainee() ;
		int              getSelectedTrainee() ;

		int              getSelectedForm() ;

		void             popupWarningMessage(String sMessage) ;
		void             popupMessage(String sMessage) ;
		void             closeWarningDialog() ;
		HasClickHandlers getWarningOk() ;

		void             popupDeleteMessage() ;
		void             closeDeleteDialog() ;
		HasClickHandlers getDeleteOk() ;
		HasClickHandlers getDeleteCancel() ;
	}

	private List<CoachingFitFormData> _aForms = new ArrayList<CoachingFitFormData>() ;
	private int                       _iFormToDelete ;

	@Inject
	public CoachingFitLoginResponsePresenter(final Display display, 
			                                 final EventBus eventBus,
			                                 final DispatchAsync dispatcher,
			                                 final CoachingFitSupervisor supervisor)
	{
		super(display, eventBus) ;

		_dispatcher = dispatcher ;
		_supervisor = supervisor ;

		_iFormToDelete = -1 ;

		_logger.info("Constructor CoachingFitLoginResponsePresenter, before createArchetypesInterfaces") ;

		createArchetypesInterfaces() ;

		initAllowedTrainees() ;

		// Deprecated (Don't call bind since it is already called by super EcogenBasePresenter)
		//
		bind() ;

		CoachingFitUser currentUser = (CoachingFitUser) _supervisor.getUser() ;
		if (currentUser.isAdministrator())
			display.showAdminPageButton() ;

		eventBus.fireEvent(new HeaderButtonsEvent(false, true)) ;

		Date tNow = new Date() ;
		display.setFormDateTo(tNow) ;

		tNow.setDate(1) ;
		tNow.setMonth(0) ;
		display.setFormDateFrom(tNow) ;
	}

	@Override
	protected void onBind() 
	{		
		_logger.info("Entering LoginResponsePresenter::onBind()") ;

		/*
		HasClickHandlers editDataHandler = display.getEditUserData() ; 
		if (null != editDataHandler)
		{
			display.getEditUserData().addClickHandler(new ClickHandler(){
				public void onClick(final ClickEvent event){
					_logger.info("Tracing!!");
					eventBus.fireEvent(new GoToUserParamsEvent()) ;
				}
			});
		}
		 */

		/*
		display.getBuildCsv().addClickHandler(new ClickHandler(){
			public void onClick(final ClickEvent event){
			  _logger.info("Building CSV");
			  buildCSV() ;
			}
		});
		 */

		eventBus.addHandler(LoginSuccessEvent.TYPE, new LoginSuccessEventHandler(){
			public void onLoginSuccess(final LoginSuccessEvent event) 
			{
				FlowPanel workSpace = event.getWorkspace() ;
				workSpace.clear() ;
				workSpace.add(getDisplay().asWidget()) ;

				eventBus.fireEvent(new HeaderButtonsEvent(false, true)) ;
				eventBus.fireEvent(new PostLoginHeaderDisplayEvent("")) ;

				refreshFormsList() ;
			}
		});

		display.getFormsSearchButton().addClickHandler(new ClickHandler(){
			public void onClick(final ClickEvent event){
				_logger.info("Refreshing the forms list");
				refreshFormsList() ;
			}
		});

		display.getEditFormButton().addClickHandler(new ClickHandler(){
			public void onClick(final ClickEvent event){
				_logger.info("Editing form");
				editForm() ;
			}
		});

		display.getDeleteFormButton().addClickHandler(new ClickHandler(){
			public void onClick(final ClickEvent event){
				_logger.info("Deleting form") ;
				if (-1 == _iFormToDelete)
				{ 
					int iSelectedPatientMessage = display.getSelectedForm() ;
					if (-1 == iSelectedPatientMessage)
					{
						display.popupWarningMessage("ERROR_MUST_SELECT_FORM") ;
						return ;
					}
					_iFormToDelete = iSelectedPatientMessage ;
					display.popupDeleteMessage() ; 
				}
			}
		});

		display.getAdminPageButton().addClickHandler(new ClickHandler(){
			public void onClick(final ClickEvent event)
			{
				_logger.info("Open administration page") ;
				eventBus.fireEvent(new GotoAdminPageEvent()) ;
			}
		});
		
		/**
		 * Reacts to Ok button in warning dialog box
		 * */
		display.getWarningOk().addClickHandler(new ClickHandler(){
			public void onClick(final ClickEvent event)
			{
				display.closeWarningDialog() ; 
			}
		});

		/**
		 * Reacts to Ok button in delete dialog box
		 * */
		display.getDeleteOk().addClickHandler(new ClickHandler(){
			public void onClick(final ClickEvent event)
			{
				display.closeDeleteDialog() ; 
				if (-1 != _iFormToDelete)
					deleteForm(_iFormToDelete) ;
			}
		});

		/**
		 * Reacts to Cancel button in delete dialog box
		 * */
		display.getDeleteCancel().addClickHandler(new ClickHandler(){
			public void onClick(final ClickEvent event)
			{
				display.closeDeleteDialog() ; 
				_iFormToDelete = -1 ;
			}
		});
	}

	/**
	 * Create the "new form" and "open dashboard" buttons and their click handlers
	 *
	 * */
	private void createArchetypesInterfaces()
	{
		// Get user and her list of archetypes descriptions
		//
		CoachingFitUser currentUser = (CoachingFitUser) _supervisor.getUser() ;

		if (null == currentUser)
			return ;

		List<ArchetypeData> aArchetypes = currentUser.getArchetypes() ;

		if ((null == aArchetypes) || aArchetypes.isEmpty())
			return ;

		// Create click handlers
		//
		_newFormClickHandler = new ClickHandler()
		{
			public void onClick(final ClickEvent event) 
			{
				Widget sender = (Widget) event.getSource() ;
				int iArchetypeId = display.getArchetypeIdForNewForm(sender) ;

				eventBus.fireEvent(new GoToNewFormEvent(iArchetypeId, null)) ;
			}
		} ;

		_openDashboardClickHandler = new ClickHandler()
		{
			public void onClick(final ClickEvent event) 
			{
				Widget sender = (Widget) event.getSource() ;
				int iArchetypeId = display.getArchetypeIdForOpenDashboard(sender) ;

				eventBus.fireEvent(new GoToOpenDashboardEvent(iArchetypeId)) ;
			}
		} ;

		_requestCsvClickHandler = new ClickHandler()
		{
			public void onClick(final ClickEvent event) 
			{
				Widget sender = (Widget) event.getSource() ;
				int iArchetypeId = display.getArchetypeIdForRequestCsv(sender) ;

				buildCSV(iArchetypeId) ;
			}
		} ;

		_showStaticHtmlClickHandler = new ClickHandler()
		{
			public void onClick(final ClickEvent event) 
			{
				Widget sender = (Widget) event.getSource() ;
				int iArchetypeId = display.getArchetypeIdForOpenStaticHtml(sender) ;

				browseStaticHtml(iArchetypeId) ;
			}
		} ;

		// Create buttons
		//
		for (ArchetypeData archetype : aArchetypes)
		{
			if      (archetype.isForm())
				display.addNewFormButton(archetype.getLabel(), _newFormClickHandler, archetype.getId()) ;
			else if (archetype.isDashboard())
				display.addOpenDashboardButton(archetype.getLabel(), _openDashboardClickHandler, archetype.getId()) ;
			else if (archetype.isCsv())
				display.addRequestCsvButton(archetype.getLabel(), _requestCsvClickHandler, archetype.getId()) ;
			else if (archetype.isStatic())
				display.addOpenStaticHtmlButton(archetype.getLabel(), _showStaticHtmlClickHandler, archetype.getId()) ;
		}

		display.showButtons() ;
	}

	/**
	 * Fill the array of allowed trainees from the coachID data in trainees information 
	 *
	 * */
	private void initAllowedTrainees()
	{
		_aAllowedTrainees.clear() ;

		CoachingFitUser user = (CoachingFitUser) _supervisor.getUser() ;
		if (null == user)
			return ;

		List<TraineeData> trainees = user.getTrainees() ;
		if ((null == trainees) || trainees.isEmpty())
			return ;

		for (TraineeData trainee : trainees)
			_aAllowedTrainees.add(trainee) ;
	}

	/**
	 * Refresh the list of already registered forms for this user 
	 *
	 * */
	private void refreshFormsListWithParameters()
	{
		int iUserId = _supervisor.getUserId() ;

		GetCoachingFormsAction formAction = new GetCoachingFormsAction() ;
		formAction.setUserId(iUserId) ;

		// If current user has a "CV" role, then restrict author to herself
		//
		if (((CoachingFitUser) _supervisor.getUser()).hasRole(0, "CV")) 
			formAction.setAuthorId(iUserId) ;
		else
		{
			int iSelectedCoach = display.getSelectedCoach() ;
			if (-1 != iSelectedCoach)
				formAction.setAuthorId(iSelectedCoach) ;
			else
			{
				int iSelectedCoachingTrainee = display.getSelectedCoachingTrainee() ;
				if (-1 != iSelectedCoachingTrainee)
					formAction.setSeniorTraineeId(iSelectedCoachingTrainee) ;
			}
		}

		// Get forms for dates
		//
		formAction.setSessionDateFrom(display.getFormDateFrom()) ;
		formAction.setSessionDateTo(display.getFormDateTo()) ;

		// Get forms for the selected trainee
		//
		int iSelectedTrainee = display.getSelectedTrainee() ;
		if (-1 != iSelectedTrainee)
			formAction.setTraineeId(iSelectedTrainee) ;

		_dispatcher.execute(formAction, new refreshFormsListWithParametersCallback()) ;
	}

	protected class refreshFormsListWithParametersCallback implements AsyncCallback<GetCoachingFormsResult> 
	{
		public refreshFormsListWithParametersCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "Unhandled error", cause);

		}//end handleFailure

		@Override
		public void onSuccess(GetCoachingFormsResult value) 
		{
			_aForms.clear() ;

			List<CoachingFitFormData> aForms = value.getForms() ;
			if (aForms.isEmpty())
			{
				refreshFormsListDisplay() ;
				return ;
			}

			for (CoachingFitFormData form : aForms)
			{
				// Check if author is current user or belongs to the managed ones before adding this form to the list
				//
				boolean bCanAdd = false ;
				if (iCurrentUserTheAuthor(form))
					bCanAdd = true ;
				else if (-1 != form.getAuthorId())
				{
					// If user is not the author, a form is displayed only if user is author's manager
					//
					UserData author = ((CoachingFitUser) _supervisor.getUser()).getCoachFromId(form.getAuthorId()) ;
					if (null != author)
						bCanAdd = true ;
				}
				// Form authored by a senior trainee
				//
				else if (-1 != form.getSeniorTraineeId())
				{
					TraineeData trainee = ((CoachingFitUser) _supervisor.getUser()).getTraineeFromId(form.getSeniorTraineeId()) ;
					if (null != trainee)
						bCanAdd = true ;
				}

				if (bCanAdd)
					_aForms.add(new CoachingFitFormData(form)) ;
			}

			// Update display
			//
			refreshFormsListDisplay() ;
		}
	}

	/**
	 * Refresh the list of already registered forms for this user 
	 *
	 * */
	private void refreshFormsList()
	{
		int iSelectedTrainee         = display.getSelectedTrainee() ;
		int iSelectedCoach           = display.getSelectedCoach() ;
		int iSelectedCoachingTrainee = display.getSelectedCoachingTrainee() ;

		if ((-1 != iSelectedTrainee) || (-1 != iSelectedCoach) || (-1 != iSelectedCoachingTrainee))
		{
			refreshFormsListWithParameters() ;
			return ;
		}

		GetCoachingFormsForUserAction formAction = new GetCoachingFormsForUserAction(_supervisor.getUserId(), ((CoachingFitSupervisor) _supervisor).getUserId(), ((CoachingFitSupervisor) _supervisor).getTraineeAsUserId()) ;
		_dispatcher.execute(formAction, new refreshFormsListCallback()) ;
	}

	protected class refreshFormsListCallback implements AsyncCallback<GetCoachingFormsForUserResult> 
	{
		public refreshFormsListCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "Unhandled error", cause);

		}//end handleFailure

		@Override
		public void onSuccess(GetCoachingFormsForUserResult value) 
		{
			// Update the list of CoachingFitFormData
			//
			_aForms.clear() ;

			List<CoachingFitFormData> aForms = value.getForms() ; 
			if (aForms.isEmpty())
				return ;

			String sDateFrom = display.getFormDateFrom() ;
			String sDateTo   = display.getFormDateTo() ;

			for (CoachingFitFormData form : aForms)
				if (("".equals(sDateFrom) || (form.getCoachingDate().compareTo(sDateFrom) >= 0)) &&
						("".equals(sDateTo)   || (form.getCoachingDate().compareTo(sDateTo) <= 0)))
					_aForms.add(new CoachingFitFormData(form)) ;

			// Update display
			//
			refreshFormsListDisplay() ;

			if (_aForms.isEmpty())
				return ;

			if ((false == _aCoachsToFind.isEmpty()) || (false == _aCoachsToFind.isEmpty()))
				fillMissingAuthors() ;
		}
	}

	/**
	 * Refresh forms list display from the list of {@link CoachingFitFormData}
	 */
	public void refreshFormsListDisplay()
	{
		display.clearFormsList() ;

		if (_aForms.isEmpty())
		{
			display.activateEditFormButton(false) ;
			display.activateDeleteFormButton(false) ;
			return ;
		}

		Collections.sort(_aForms) ;
		Collections.reverse(_aForms) ;

		List<String> aMissingDictEntries = new ArrayList<String>() ;

		for (CoachingFitFormData form : _aForms)
			display.addForm(getFormLabel(form, aMissingDictEntries), form.getFormId()) ;

		display.activateEditFormButton(true) ;
		display.activateDeleteFormButton(true) ;

		if (false == aMissingDictEntries.isEmpty())
		{
			List<Dictionary> aFoundEntries = _supervisor.getDataDictionaryProxy().getDictionnariesFromExactCodes(aMissingDictEntries, new getArchetypeRootCallback(), _supervisor.getUserLanguage(), _supervisor.getUserId()) ;

			// We must do this since, if all missing entries suddenly appeared in the buffer, the callback won't get called
			//
			if ((null != aFoundEntries) && (aFoundEntries.size() == aMissingDictEntries.size()))
				refreshFormsListDisplay() ;
		}
	}

	/**
	 * Is current user the author of a given form? 
	 */
	protected boolean iCurrentUserTheAuthor(final CoachingFitFormData form)
	{
		// Usual case, user is not a trainee
		//
		if (false == _supervisor.isUserATrainee())
			return (form.getAuthorId() == _supervisor.getUser().getUserData().getId()) ;

		// Less usual case, user is a trainee
		//
		return (form.getSeniorTraineeId() == _supervisor.getTraineeUser().getId()) ;
	}

	protected void mailForm()
	{

	}

	private void editForm()
	{
		int iSelectedForm = display.getSelectedForm() ;
		if (-1 == iSelectedForm)
		{
			display.popupWarningMessage("ERROR_MUST_SELECT_FORM") ;
			return ;
		}

		eventBus.fireEvent(new GoToEditFormEvent(iSelectedForm)) ;
	}

	private void deleteForm(int iFormToDelete) 
	{
		_dispatcher.execute(new DeleteFormAction(_supervisor.getUserId(), iFormToDelete), new deleteFormCallback()) ;
	}

	protected class deleteFormCallback implements AsyncCallback<DeleteFormResult> 
	{
		public deleteFormCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "Unhandled error", cause) ;

		}//end handleFailure

		@Override
		public void onSuccess(DeleteFormResult value) 
		{
			String sErrorMessage = value.getMessage() ;
			if ((null != sErrorMessage) && (false == sErrorMessage.equals("")))
				display.popupMessage(sErrorMessage) ;

			refreshFormsList() ;

			_iFormToDelete = -1 ;
		}
	}

	private void buildCSV(final int iArchetypeId)
	{
		_dispatcher.execute(new GetCsvAction(_supervisor.getUserId(), iArchetypeId), new buildCSVCallback()) ;
	}

	protected class buildCSVCallback implements AsyncCallback<GetCsvResult> 
	{
		public buildCSVCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "Unhandled error", cause);

		}//end handleFailure

		@Override
		public void onSuccess(GetCsvResult value) 
		{
			String sErrorMessage = value.getMessage() ;
			if (false == sErrorMessage.equals(""))
				display.popupMessage(sErrorMessage) ;
			else
				display.popupMessage("Fichier CSV disponible") ;
		}
	}

	/**
	 * Build the label for a form
	 */
	protected String getFormLabel(final CoachingFitFormData formData, List<String> aMissingDictEntries)
	{
		if (null == formData)
			return "" ;

		CoachingFitUser user = (CoachingFitUser) _supervisor.getUser() ;
		if (null == user)
			return "" ;

		String sLabel = "" ;

		// If draft, put it clearly
		//
		if (formData.isDraft())
			sLabel += "*Draft* " ;

		// Get label for ROOT
		//
		String sRoot = formData.getRoot() ;
		if (false == sRoot.isEmpty())
		{
			Dictionary dico = _supervisor.getDataDictionaryProxy().getDictionnaryFromExactCodeFromBuffer(sRoot, _supervisor.getUserLanguage()) ;

			// If some flex are synchronously returned, process them immediately
			//
			if (null != dico)
				sLabel += dico.getLabel() + " " ;
			else
			{
				if (false == aMissingDictEntries.contains(sRoot))
					aMissingDictEntries.add(sRoot) ;
			}
		}

		// Get trainee label
		//
		TraineeData traineeData = user.getTraineeFromId(formData.getTraineeId()) ;
		if (null != traineeData)
			sLabel += traineeData.getLabel() ;

		// If the author is not current user, display it
		//
		if (formData.getAuthorId() != user.getUserData().getId())
		{
			if (formData.getAuthorId() > 0)
			{
				UserData author = user.getCoachFromId(formData.getAuthorId()) ;
				if (null != author)
					sLabel += " par " + author.getLabel() ;
				else
				{
					sLabel += " par auteur inconnu (" + formData.getAuthorId() + ")" ;
					_aCoachsToFind.add(formData.getAuthorId()) ;
				}
			}
			else if (formData.getSeniorTraineeId() > 0)
			{
				TraineeData seniorTraineeData = user.getTraineeFromId(formData.getSeniorTraineeId()) ;
				if (null != seniorTraineeData)
					sLabel += " par " + seniorTraineeData.getLabel() ;
				else
				{
					sLabel += " par auteur inconnu (CV senior " + formData.getSeniorTraineeId() + ")" ;
					_aTraineesToFind.add(formData.getAuthorId()) ;
				}
			}
			else
				sLabel += " par auteur non référencé" ;
		}
		else
			sLabel += " par vous" ;

		// Get date
		//
		String sDate = formData.getCoachingDate() ;
		if ((null != sDate) && (false == "".equals(sDate)))
		{
			sLabel += " - " + sDate.substring(6, 8) + "/" + sDate.substring(4, 6) + "/" + sDate.substring(0, 4) ;
		}

		return sLabel ;
	}

	protected class getArchetypeRootCallback implements DataDictionaryCallBack
	{
		public getArchetypeRootCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "Unhandled error", cause);
		}//end handleFailure

		@Override
		public boolean onSuccess()
		{
			refreshFormsListDisplay() ;
			return true ;
		}
	}

	protected void fillMissingAuthors()
	{
		if      (false == _aCoachsToFind.isEmpty())
			addMissingCoaches() ;
		else if (false == _aTraineesToFind.isEmpty())
			addMissingTrainees() ;
	}

	protected void addMissingTrainees() {
		_dispatcher.execute(new GetCoachingFitTraineesListAction(_supervisor.getUserId(), _aTraineesToFind), new addMissingTraineesCallback()) ;
	}

	protected class addMissingTraineesCallback implements AsyncCallback<GetCoachingFitTraineesListResult> 
	{
		public addMissingTraineesCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "Unhandled error", cause);

		}//end handleFailure

		@Override
		public void onSuccess(GetCoachingFitTraineesListResult value) 
		{
			List<TraineeData> aTrainees = value.getTraineesData() ;
			if ((null != aTrainees) && (false == aTrainees.isEmpty()))
			{
				CoachingFitUser currentUser = (CoachingFitUser) _supervisor.getUser() ;
				if (null == currentUser)
					return ;

				for (TraineeData trainee : aTrainees)
					currentUser.addTrainee(new TraineeData(trainee)) ;
			}

			if (false == _aCoachsToFind.isEmpty())
				addMissingCoaches() ;
			else
				refreshFormsListDisplay() ;
		}
	}

	protected void addMissingCoaches() {
		_dispatcher.execute(new GetCoachingFitCoachsListAction(_supervisor.getUserId(), _aCoachsToFind), new addMissingCoachsCallback()) ;
	}

	protected class addMissingCoachsCallback implements AsyncCallback<GetCoachingFitCoachsListResult> 
	{
		public addMissingCoachsCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "Unhandled error", cause);

		}//end handleFailure

		@Override
		public void onSuccess(GetCoachingFitCoachsListResult value) 
		{
			List<UserData> aCoachs = value.getCoachsData() ;
			if ((null != aCoachs) && (false == aCoachs.isEmpty()))
			{
				CoachingFitUser currentUser = (CoachingFitUser) _supervisor.getUser() ;
				if (null == currentUser)
					return ;

				for (UserData coach : aCoachs)
					currentUser.addCoach(new UserData(coach)) ;
			}

			_aCoachsToFind.clear() ;

			if (false == _aTraineesToFind.isEmpty())
				addMissingTrainees() ;
			else
				refreshFormsListDisplay() ;
		}
	}

	protected void browseStaticHtml(int iArchetypeId)
	{
		if (-1 == iArchetypeId)
			return ;


		// Get user and her list of archetypes descriptions
		//
		CoachingFitUser currentUser = (CoachingFitUser) _supervisor.getUser() ;

		if (null == currentUser)
			return ;

		String sFileName = "" ;

		List<ArchetypeData> aArchetypes = currentUser.getArchetypes() ;
		for (ArchetypeData archetype : aArchetypes)
			if (archetype.getId() == iArchetypeId)
				sFileName = archetype.getFile() ;

		if ("".equals(sFileName))
			return ;

		String sUrl = Window.Location.createUrlBuilder().setPath("./" + sFileName).buildString() ;
		Window.open(sUrl, "_blank", null) ;
	}

	@Override
	protected void onUnbind() {
		// Add unbind functionality here for more complex presenters
	}

	@Override
	public void revealDisplay() {
		// nothing to do, there is more useful in UI which may be buried
		// in a tab bar, tree, etc.
	}

	@Override
	protected void onRevealDisplay() {
		// TODO Auto-generated method stub
	}
}
