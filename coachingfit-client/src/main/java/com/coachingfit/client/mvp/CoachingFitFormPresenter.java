package com.coachingfit.client.mvp;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.coachingfit.client.global.CoachingFitSupervisor;
import com.coachingfit.client.widgets.FitScoreBase;
import com.coachingfit.client.widgets.GlobalExpectedScoreControl;
import com.coachingfit.client.widgets.GlobalFitScoreControl;
import com.coachingfit.shared.database.ArchetypeForJobData;
import com.coachingfit.shared.database.CoachingFitFormData;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.model.CoachingFitUser;
import com.coachingfit.shared.rpc.GetCoachingFitJobs4ArchetypeAction;
import com.coachingfit.shared.rpc.GetCoachingFitJobs4ArchetypeResult;
import com.coachingfit.shared.rpc.GetCoachingFitPreviousFormBlockAction;
import com.coachingfit.shared.rpc.GetCoachingFitPreviousFormBlockResult;
import com.coachingfit.shared.rpc.GetCoachingFitTraineeAction;
import com.coachingfit.shared.rpc.GetCoachingFitTraineeResult;
import com.coachingfit.shared.rpc.GetCoachingFormsAction;
import com.coachingfit.shared.rpc.GetCoachingFormsResult;
import com.coachingfit.shared.rpc.SendFormByMailAction;
import com.coachingfit.shared.rpc.SendFormByMailResult;
import com.coachingfit.shared.util.CoachingFitDate;
import com.coachingfit.shared.util.CoachingFitDelay;
import com.coachingfit.shared.util.MiscellanousFcts;
import com.coachingfit.shared.util.MiscellanousFcts.STRIP_DIRECTION;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import com.primege.client.event.EditFormEvent;
import com.primege.client.event.GoToLoginResponseEvent;
import com.primege.client.mvp.FormInterfaceModel;
import com.primege.client.mvp.FormPresenterInterface;
import com.primege.client.mvp.FormPresenterModel;
import com.primege.client.util.FormControl;
import com.primege.client.util.FormControlOptionData;
import com.primege.client.widgets.ControlBase;
import com.primege.client.widgets.EventDateControl;
import com.primege.client.widgets.FormBlockPanel;
import com.primege.client.widgets.FormListBox;
import com.primege.client.widgets.FormTextArea;
import com.primege.shared.database.FormDataData;
import com.primege.shared.database.FormDataModel;
import com.primege.shared.model.FormBlock;
import com.primege.shared.model.FormBlockModel;
import com.primege.shared.rpc.GetFormBlockAction;
import com.primege.shared.rpc.GetFormBlockResult;
import com.primege.shared.rpc.RegisterFormAction;
import com.primege.shared.util.MailTo;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;

public class CoachingFitFormPresenter extends FormPresenterModel<CoachingFitFormPresenter.Display> implements FormPresenterInterface
{
	public interface Display extends FormInterfaceModel
	{
		void                             setTrainees(List<TraineeData> trainees) ;
		void                             setSeniority(final String sSeniority) ;
		HasChangeHandlers                getTraineeChanged() ;

		GlobalFitScoreControl            getGlobalFitScoreControl() ;
		GlobalExpectedScoreControl       getGlobalExpectedScoreControl() ;

		List<GlobalFitScoreControl>      getSubGlobalFitScoreControls() ;
		List<GlobalExpectedScoreControl> getSubExpectedFitScoreControls() ;

		String                           getWorkspaceHtmlContent() ;
	}

	private List<String> _aFitScorePaths ;
	private int          _iLastSavedTrainee ;
	private String       _sTraineeSeniority ;

	/** Maximum possible score */
	private double       _dTotalWeightsCount ;
	/** Maximum score for trainee seniority level */
	private double       _dExpectedWeightsCount ;

	private String       _sMailBody ;

	@Inject
	public CoachingFitFormPresenter(final Display               display, 
			                        final EventBus              eventBus,
			                        final DispatchAsync         dispatcher,
			                        final CoachingFitSupervisor supervisor)
	{
		super(display, eventBus, dispatcher, supervisor) ;

		_aFitScorePaths = new ArrayList<String>() ;

		_iLastSavedTrainee  = 0 ;
		_sTraineeSeniority  = "" ;
		_sMailBody          = "" ;

		_dTotalWeightsCount    = 0 ;
		_dExpectedWeightsCount = 0 ;
	}

	@Override
	protected void onBind() 
	{
		_logger.log(Level.CONFIG, "Entering CoachingFitFormPresenter::onBind()") ;
		super.onBind() ;

		/**
		 * submit user registration information
		 * */
		display.getSubmitButton().addClickHandler(new ClickHandler()
		{
			public void onClick(final ClickEvent event)
			{
				_bSavingDraft = false ;

				SubmitForm() ;
			}
		}) ;

		/**
		 * submit a draft of user registration information
		 * */
		display.getSubmitDraftButton().addClickHandler(new ClickHandler()
		{
			public void onClick(final ClickEvent event)
			{
				_bSavingDraft = true ;

				SubmitForm() ;
			}
		}) ;
	}

	/**
	 * Reset the screen and set default values
	 * 
	 * */
	public void prepareSreen()
	{
		resetAll(false) ;

		display.setDefaultValues() ;
	}

	/**
	 * This reset function is usually to be super-charged by super-classes, specifically to reset the display
	 * 
	 * Take care that this function is called in constructor during super-class construction, hence before local constructor can initialize variables
	 * 
	 * */
	public void resetAll(boolean bScreenShotMode)
	{
		super.resetAll(bScreenShotMode) ;

		if (null != _aFitScorePaths)
			_aFitScorePaths.clear() ;

		_iLastSavedTrainee     = 0 ;
		_sTraineeSeniority     = "" ;
		_dTotalWeightsCount    = 0 ;
		_dExpectedWeightsCount = 0 ;

		display.resetAll() ;
	}

	/**
	 * Add an interface control to the display
	 * 
	 * @param sControlPath    control's path (arborescent identifier)
	 * @param content         control's description
	 * @param sControlCaption control's caption
	 * @param sControlType    control's type (Edit, Buttons...)
	 * @param sControlSubtype control's sub-type (for example FreeText or Number for Edit control)
	 * @param sControlUnit    control's unit for numbers
	 * @param sControlValue   control's initialization value
	 * @param aOptions        list of options
	 * @param sControlStyle   CSS style for the control
	 * @param sCaptionStyle   CSS style for the label
	 * @param bInitFromPrev   <code>true</code> if the process is to initialize information from the previous form
	 * @param bInBlockCell    <code>true</code> if the control is to be inserted in the block, <code>false</code> if in label's cell
	 * 
	 */
	public void insertNewControl(final String sControlPath, final String sControlCaption, final String sControlType, final String sControlSubtype, final String sControlUnit, final String sControlValue, final List<FormControlOptionData> aOptions, final String sControlStyle, final String sCaptionStyle, final boolean bInitFromPrev, final String sExclusion, final boolean bInBlockCell, final boolean bInPdfWhenEmpty, FormBlockPanel masterBlock, FormBlockModel<FormDataData> aInformation) 
	{
		boolean bEdited = (null != aInformation) ;

		if ("FitScore".equalsIgnoreCase(sControlType) || "FitScorePlus".equalsIgnoreCase(sControlType) || "TenScore".equalsIgnoreCase(sControlType) || "TenScorePlus".equalsIgnoreCase(sControlType)) 
			_aFitScorePaths.add(sControlPath) ;

		display.insertNewControl(sControlPath, getEditedInformationForPath(sControlPath, aOptions, aInformation), sControlCaption, sControlType, sControlSubtype, sControlUnit, sControlValue, aOptions, sControlStyle, sCaptionStyle, bInitFromPrev, sExclusion, bInBlockCell, bInPdfWhenEmpty, masterBlock, bEdited) ;
	}

	/**
	 * Provide the view with a list of trainees that is consistent with coach and current archetype
	 */
	protected void fixTraineesList()
	{
		// Get the list of jobs types for this archetype
		//
		CoachingFitSupervisor CSSuper = (CoachingFitSupervisor) _supervisor ;
		if (null == CSSuper)
			return ;

		GetCoachingFitJobs4ArchetypeAction action = new GetCoachingFitJobs4ArchetypeAction(CSSuper.getUserId(), _iArchetypeId) ; 

		_dispatcher.execute(action, new FixTraineeListCallback()) ;
	}

	/**
	 * Callback method for trainees list query
	 */
	protected class FixTraineeListCallback implements AsyncCallback<GetCoachingFitJobs4ArchetypeResult> 
	{
		public FixTraineeListCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "Unhandled error in FixTraineeListCallback", cause) ;
			display.setTrainees(((CoachingFitSupervisor) _supervisor).getTraineesForUser()) ;
		}

		@Override
		public void onSuccess(GetCoachingFitJobs4ArchetypeResult value) 
		{
			List<TraineeData> aTraineesForUser = ((CoachingFitSupervisor) _supervisor).getTraineesForUser() ;

			// If there is no jobs types returned for current archetype, then keep the full trainees list
			//
			if ((null == value) || (value.getJobsTypes().isEmpty()) || aTraineesForUser.isEmpty())
			{
				display.setTrainees(aTraineesForUser) ;
				return ;
			}

			List<TraineeData> aSelectedTrainees = new ArrayList<TraineeData>() ; 

			for (ArchetypeForJobData jobType : value.getJobsTypes())
			{
				String sJobType = jobType.getJobType() ;

				// If job type contains a '*', it means that it is to be taken as a model
				//
				int iJoker = sJobType.indexOf('*') ;

				// No Joker, add trainees with exact match
				//
				if (-1 == iJoker)
				{
					for (TraineeData trainee : aTraineesForUser)
						if (sJobType.equals(trainee.getJobType()))
							addTrainee(aSelectedTrainees, trainee) ;
				}
				else
				{
					String sModel = "" ;
					if (iJoker > 0)
						sModel = sJobType.substring(0, iJoker) ;

					for (TraineeData trainee : aTraineesForUser)
						if ((trainee.getJobType().length() >= iJoker) && (trainee.getJobType().substring(0, iJoker).equals(sModel)))
							addTrainee(aSelectedTrainees, trainee) ;
				}
			}

			display.setTrainees(aSelectedTrainees) ;
		}

		/**
		 * Add a trainee in the List, if not already present
		 * 
		 * @param aSelectedTrainees ArrayList to have new trainee added to
		 * @param trainee           Trainee to add
		 */
		protected void addTrainee(List<TraineeData> aSelectedTrainees, final TraineeData trainee)
		{
			if ((null == trainee) || (null == aSelectedTrainees))
				return ;

			// Check if this trainee already exists in list
			//
			if (aSelectedTrainees.contains(trainee))
				return ;

			aSelectedTrainees.add(new TraineeData(trainee)) ;
		}
	}

	/**
	 * This object intercepts clicks on the "save form" button
	 */
	protected void SubmitForm()
	{
		if (_bSaveInProgress)
			return ;

		// If we know that a form already exist in database, we shouldn't save it twice
		//
		if (_bFormAlreadyExist)
		{
			display.popupWarningMessage("WARNING_FORM_ALREADY_EXIST") ;
			return ;
		}
		// if (_bCheckingFormExist)
		//	return ;

		List<FormDataData> aFormInformation = new ArrayList<FormDataData>() ;
		boolean bAllControlFilled = display.getContent(aFormInformation, null) ;

		// If a control is not filled or invalid, we refuse to save (except if a draft)
		//
		if ((false == bAllControlFilled) && (false == _bSavingDraft))
		{
			display.popupWarningMessage("ERROR_MUST_ENTER_EVERY_INFORMATION") ;
			return ;
		}

		// Do it now, before most information is removed
		//
		if (false == _bSavingDraft)
			_sMailBody = buildMailBody() ;

		// Prepare information to save document's label
		//
		int    iTraineeId    = getIntValueForPath("$trainee$", null) ;
		int    iRegionId     = getRegionForTrainee(iTraineeId) ;
		String sCoachingDate = getValueForPath("$date$", null) ;

		_iLastSavedTrainee   = iTraineeId ;

		int iUserId          = _supervisor.getUserId() ;
		int iSeniorTraineeId = ((CoachingFitSupervisor) _supervisor).getTraineeAsUserId() ;

		FormDataModel.FormStatus iStatus = FormDataModel.FormStatus.valid ;
		if (_bSavingDraft)
			iStatus = FormDataModel.FormStatus.draft ;

		// TODO Check if we can get the edited root (in case it is not a new form)
		String sRoot = "" ;

		CoachingFitFormData formData = new CoachingFitFormData(_iFormId, "", sRoot, iTraineeId, iRegionId, sCoachingDate, iUserId, iSeniorTraineeId, _sRecordDate, _iArchetypeId, iStatus) ;

		// Remove information that are dedicated to document's label
		//
		removeFromInformation(aFormInformation, "$trainee$") ;
		removeFromInformation(aFormInformation, "$date$") ;

		FormBlock<FormDataData> formBlock = new FormBlock<FormDataData>("", formData, aFormInformation) ;

		if (areDataOk(formBlock))
		{
			_bSaveInProgress = true ;
			display.showWaitCursor() ;
			_dispatcher.execute(new RegisterFormAction(_supervisor.getUserId(), formBlock, _aTraits), new RegisterFormCallback()) ;
		}
	}

	/**
	 * Create handlers that react to information change in form<br>
	 * <br>
	 * This function is called from FormPresenterModel:initFromArchetype()
	 * <br>
	 * It is there since trainee is a form parameter, hence checking for an already existing form for this user at this date
	 * must be done each time user and/or date is modified. 
	 */
	public void createChangeHandlers(FormBlockPanel masterBlock)
	{
		if (display.isScreenShotMode())
			return ;

		initFormDependantInformation() ;

		FormBlockPanel referenceBlock = display.getMasterForm() ; ;

		// Change handler to check if ongoing report doesn't already exist in database
		//
		_CheckExistChangeHandler = new ChangeHandler()
		{
			public void onChange(final ChangeEvent event) 
			{
				int    iTraineeId = getIntValueForPath("$trainee$", null) ;
				String sEventDate = getValueForPath("$date$", null) ;

				//
				//
				if ((-1 == iTraineeId) || "".equals(sEventDate))
					return ;

				// In case we are editing a form that is already saved, we must first check
				// if one of the document label information has changed
				//
				if (null != referenceBlock.getEditedBlock())
				{
					CoachingFitFormData documentLabel = (CoachingFitFormData) referenceBlock.getEditedBlock().getDocumentLabel() ;
					if (null != documentLabel)
					{
						if ((documentLabel.getTraineeId() == iTraineeId) &&
								documentLabel.getCoachingDate().equals(sEventDate))
						{
							_bFormAlreadyExist = false ;
							return ;
						}
					}
				}

				// Asking the server if a form already exists for the trainee at the given date
				//
				GetCoachingFormsAction getFormsAction = new GetCoachingFormsAction() ;
				getFormsAction.setUserId(_supervisor.getUserId()) ;

				getFormsAction.setSessionDateFrom(sEventDate) ;
				getFormsAction.setSessionDateTo(sEventDate) ;
				getFormsAction.setTraineeId(iTraineeId) ;
				getFormsAction.setArchetypeId(_iArchetypeId) ;

				_bCheckingFormExist = true ;

				_dispatcher.execute(getFormsAction, new CheckExistFormCallback()) ;
			}
		} ;

		// If the trainee or the session date are modified, it must fire the check exist handler
		//
		HasChangeHandlers traineeChngHandler = display.getTraineeChanged() ;
		if (null != traineeChngHandler)
			traineeChngHandler.addChangeHandler(_CheckExistChangeHandler) ;

		HasChangeHandlers dateChngHandler = display.getDateChanged() ;
		if (null != dateChngHandler)
			dateChngHandler.addChangeHandler(_CheckExistChangeHandler) ;

		connectFitScoresClickHandlers() ;
	}

	/**
	 * Asking the server if a form already exists for the trainee at the given date
	 * @author Philippe
	 *
	 */
	protected class CheckExistFormCallback implements AsyncCallback<GetCoachingFormsResult> 
	{
		public CheckExistFormCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "Unhandled error in CheckExistFormCallback", cause) ;
		}

		@Override
		public void onSuccess(GetCoachingFormsResult value) 
		{
			if ((null != value) && (false == value.getForms().isEmpty()))
			{
				_bFormAlreadyExist = true ;			
				display.popupWarningMessage("WARNING_FORM_ALREADY_EXIST") ;
			}
			else
				_bFormAlreadyExist = false ;

			_bCheckingFormExist = false ;

			initTraineeDependantInformation() ;
		}
	}

	/**
	 * Initialize pre-processed information 
	 */
	protected void initFormDependantInformation()
	{
		// Process global weights count
		//
		processGlobalWeightsCount() ;
	}

	/**
	 * Initialize pre-processed information 
	 */
	protected void initTraineeDependantInformation()
	{
		// Initialize trainee's seniority information
		//
		_sTraineeSeniority = getTraineeSeniority() ;
		display.setSeniority(_sTraineeSeniority) ;

		// Display global expected information
		//
		processGlobalExpectedScore() ;
	}

	/**
	 * Get trainee's seniority
	 * 
	 * @return Seniority as a YYMMSS string if all went well, <code>""</code> if not
	 */
	protected String getTraineeSeniority()
	{
		String sEventDate = getValueForPath("$date$", null) ;
		if ("".equals(sEventDate))
			return "" ;

		// Get trainee's job starting date
		//
		int iTraineeId = getIntValueForPath("$trainee$", null) ;

		CoachingFitUser user = (CoachingFitUser) _supervisor.getUser() ;
		TraineeData traineeInformation = user.getTraineeFromId(iTraineeId) ;

		if (null == traineeInformation)
			return "" ;

		String sJobStartDate = traineeInformation.getJobStartDate() ;
		if ("".equals(sJobStartDate))
			return "" ;

		CoachingFitDelay delay = new CoachingFitDelay() ;
		delay.initFromDateInterval(new CoachingFitDate(sJobStartDate), new CoachingFitDate(sEventDate)) ;

		return delay.getAsString() ;
	}

	/**
	 * Get information in the edited block from a path.<br>
	 * <br>
	 * This method supercharges FormPresenterModel.getEditedInformationForPath. Take care to keep its signature consistent with the model.
	 *
	 * @param sPath    Path of the control to get information from
	 * @param aOptions Control options
	 * @param aInformation Information to look into for path
	 *
	 */
	protected List<FormDataData> getEditedInformationForPath(final String sPath, final List<FormControlOptionData> aOptions, FormBlockModel<FormDataData> aInformation)
	{
		if ((null == sPath) || "".equals(sPath))
			return null ;

		// Artificial paths
		//
		if ("$trainee$".equals(sPath) || "$date$".equals(sPath))
		{
			if (null == aInformation)
				return null ;

			CoachingFitFormData formData = (CoachingFitFormData) ((FormBlock<FormDataData>) aInformation).getDocumentLabel() ;
			if (null == formData)
				return null ;

			FormDataData fakeInformation = new FormDataData() ;
			if ("$trainee$".equals(sPath))
				fakeInformation.setValue(Integer.toString(formData.getTraineeId())) ;
			if ("$date$".equals(sPath))
				fakeInformation.setValue(formData.getCoachingDate()) ;

			List<FormDataData> aContent = new ArrayList<FormDataData>() ;
			aContent.add(fakeInformation) ;

			return aContent ;
		}

		return getInformationForRegularPath(aInformation, sPath, aOptions) ;		
	}

	/**
	 * In edit mode, fill the form with information to be edited
	 * 
	 */
	public void initFromExistingInformation()
	{
		if (-1 == _iFormId)
			return ;

		_dispatcher.execute(new GetFormBlockAction(_supervisor.getUserId(), _iFormId), new editFormCallback()) ;
	}

	protected class editFormCallback implements AsyncCallback<GetFormBlockResult> 
	{
		public editFormCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "Unhandled error in editForm", cause) ;
		}//end handleFailure

		@Override
		public void onSuccess(GetFormBlockResult value) 
		{
			String sServerMsg = value.getMessage() ;
			if (false == sServerMsg.isEmpty())
			{
				_logger.log(Level.SEVERE, "Error when getting form from server (" + sServerMsg + ").") ;
				return ;
			}

			setEditedBlock(new FormBlock<FormDataData>(value.getFormBlock())) ;
			initFromBlock((FormBlock<FormDataData>) getEditedBlock()) ;
		}
	}

	/**
	 * Opens the proper archetype to load in order to initialize it from a block of information 
	 */
	protected void initFromBlock(FormBlock<FormDataData> block)
	{
		if (null == block)
			return ;

		CoachingFitFormData formData = (CoachingFitFormData) block.getDocumentLabel() ;
		if (null == formData)
			return ;

		_sRecordDate  = formData.getEntryDateHour() ;
		_iArchetypeId = formData.getArchetypeId() ;

		// Before getting the archetype (and displaying information on screen), we must check if the trainee
		// exists in supervisor's library. It may not be the case if this trainee no longer belongs to current user's team. 
		//
		int iTraineeId = formData.getTraineeId() ;
		if (iTraineeId > 0)
		{
			_iLastSavedTrainee = iTraineeId ; 

			CoachingFitUser user = (CoachingFitUser) _supervisor.getUser() ;
			TraineeData traineeInformation = user.getTraineeFromId(iTraineeId) ;
			if (null == traineeInformation)
			{
				getTraineeInformation(iTraineeId) ;
				return ;
			}
		}

		getArchetype() ;
	}

	/**
	 * Add a click handler to every fit score controls
	 */
	protected void connectFitScoresClickHandlers()
	{
		if ((null == _aFitScorePaths) || _aFitScorePaths.isEmpty())
			return ;

		for (String sPath : _aFitScorePaths)
		{
			Widget widgetForPath = display.getControlForPath(sPath, null) ;
			if (null != widgetForPath)
			{
				if (widgetForPath instanceof FitScoreBase)
				{
					final FitScoreBase control = (FitScoreBase) widgetForPath ;

					control.addClickHandler(new ClickHandler() {
						public void onClick(ClickEvent event) 
						{
							if (false == control.isReadOnly())
								refreshScores() ;
						}
					});
				}
			}
		}
	}

	/**
	 * A fit score was checked, adapt score(s)
	 */
	protected void refreshScores()
	{
		// Global weight can be modified when controls are set to "not assessable
		//
		processGlobalWeightsCount() ;

		// Expected score can be modified by seniority and when controls are set to "not assessable
		//
		processGlobalExpectedScore() ;

		// The global score depends on global weight count if set to "percent" mode
		//
		refreshGlobalScore() ;
	}

	/**
	 * A fit score was checked, adapt global score(s)
	 */
	protected void refreshGlobalScore()
	{
		// Find the control in charge of displaying the global score
		//
		GlobalFitScoreControl totalScore = display.getGlobalFitScoreControl() ;

		// Get controls in charge of displaying total scores for blocks
		//
		List<GlobalFitScoreControl> aSubTotals = display.getSubGlobalFitScoreControls() ;

		if ((null == totalScore) && aSubTotals.isEmpty())
			return ;

		// Reset global score controls
		//

		// Reset the global score
		//
		String sTargetRoot = "" ;

		if (null != totalScore)
		{
			totalScore.setContent(null, "") ;

			sTargetRoot = totalScore.getTargetRoot() ;
		}

		// List of block score paths in order to keep only the ones which scores are all activated
		//
		List<String> aBlocsPaths = new ArrayList<String>() ;

		// Reset all block global scores and add their paths to the list
		//
		if (false == aSubTotals.isEmpty())
			for (GlobalFitScoreControl blockScore : aSubTotals)
			{
				blockScore.setContent(null, "") ;
				aBlocsPaths.add(blockScore.getControlBase().getPath()) ;
			}

		// If no fit score controls, nothing to do now that global scores are reseted  
		//
		if ((null == _aFitScorePaths) || _aFitScorePaths.isEmpty())
			return ;

		// The rule is that the global score can only been displayed if all fit scores are set
		//
		boolean bMissingScores = false ;

		// Process the new scores
		//
		double dScore = 0 ;

		for (String sPath : _aFitScorePaths)
		{
			Widget widgetForPath = display.getControlForPath(sPath, null) ;
			if (null != widgetForPath)
			{
				if (widgetForPath instanceof FitScoreBase)
				{
					FitScoreBase control = (FitScoreBase) widgetForPath ;

					// Is this control involved in the global score?
					//
					boolean bValidForGlobal = ("".equals(sTargetRoot) || sPath.startsWith(sTargetRoot)) ;

					// Look for a block score to increment
					//
					GlobalFitScoreControl blockScore = getBlockGlobalScore(control.getControlBase()) ;

					if (control.isActivated())
					{
						double dFitScore = control.getContentAsDouble() ;

						if (bValidForGlobal)
							dScore += dFitScore ;

						if (null != blockScore)
							blockScore.addToContent(dFitScore) ;
					}
					// If a control is empty, then we cannot process the global score
					//
					else
					{
						if (bValidForGlobal)
							bMissingScores = true ;

						// If there is a block score for this control, we reset it and remove its path from the list
						//
						if ((null != blockScore) && aBlocsPaths.contains(blockScore.getControlBase().getPath()))
						{
							blockScore.setContent(null, "") ;
							aBlocsPaths.remove(blockScore.getControlBase().getPath()) ;
						}
					}
				}
			}
		}

		if (bMissingScores)
			return ;

		if (null != totalScore)
		{
			// Adjust so the score is "base 100"
			//
			if      (GlobalFitScoreControl.MODE.percentFromGlobal == totalScore.getMode())
				dScore = getWeightedScoreFromGlobal(dScore) ;
			else if (GlobalFitScoreControl.MODE.percentFromExpected == totalScore.getMode())
				dScore = getWeightedScoreFromExpected(dScore) ;

			totalScore.setValue(dScore) ;
		}
	}

	/**
	 * Initializes _dTotalWeightsCount, the global weights count
	 * 
	 * The rule is that each FitScoreControl with no explicit weight is worth 1
	 * Control have a weight of 0 is they are set to "non assessable"
	 */
	protected void processGlobalWeightsCount()
	{
		_dTotalWeightsCount = 0 ;

		if ((null == _aFitScorePaths) || _aFitScorePaths.isEmpty())
			return ;

		// Find the control in charge of displaying the global score in order to know if it targets a specific path
		//
		String sTargetRoot = "" ;
		GlobalFitScoreControl totalScore = display.getGlobalFitScoreControl() ;
		if (null != totalScore)
			sTargetRoot = totalScore.getTargetRoot() ;

		// Browse all fit score controls and take into account the ones that are involved in the global score
		//
		for (String sPath : _aFitScorePaths)
		{
			// Is this control involved in the global score?
			//
			boolean bValidForGlobal = ("".equals(sTargetRoot) || sPath.startsWith(sTargetRoot)) ;

			if (bValidForGlobal)
			{
				Widget widgetForPath = display.getControlForPath(sPath, null) ;
				if (null != widgetForPath)
				{
					if (widgetForPath instanceof FitScoreBase)
					{
						FitScoreBase control = (FitScoreBase) widgetForPath ;
						_dTotalWeightsCount += control.getWeightAsDouble() ;
					}
					else
						_dTotalWeightsCount += 1 ;
				}
			}
		}
	}

	/**
	 * Get a "base 100" score from the raw count compared to the global score
	 */
	protected double getWeightedScoreFromGlobal(final double dScore) {
		return getWeightedScore(dScore, _dTotalWeightsCount) ;
	}

	/**
	 * Get a "base 100" score from the raw count compared to the expected score
	 */
	protected double getWeightedScoreFromExpected(final double dScore) {
		return getWeightedScore(dScore, _dExpectedWeightsCount) ;
	}

	/**
	 * Get a "base 100" score from the raw count
	 */
	protected double getWeightedScore(final double dScore, final double dComparedTo)
	{
		if (100 == dComparedTo)
			return dScore ;

		// No global score available, count each entry element as "1 point"
		//
		if (0 == dComparedTo)
			return dScore * 100 / _aFitScorePaths.size() ;

		return dScore * 100 / dComparedTo ;
	}

	/**
	 * Initializing information than depends on seniority
	 */
	protected void processGlobalExpectedScore()
	{
		_dExpectedWeightsCount = 0 ;

		// Find the control in charge of displaying the global score
		//
		GlobalExpectedScoreControl expectedScore = display.getGlobalExpectedScoreControl() ;

		// Check if it targets a specific path
		//
		String sTargetRoot = "" ;
		if (null != expectedScore)
			sTargetRoot = expectedScore.getTargetRoot() ;

		// Reset the global expected score control
		//
		if (null != expectedScore)
			expectedScore.setContent(null, "") ;

		if ((null == _aFitScorePaths) || _aFitScorePaths.isEmpty())
			return ;

		// List of already encountered block score paths in order to reset them during first encounter
		//
		List<String> aExpectedBlocsScores = new ArrayList<String>() ;

		// Iterate on fit score controls in order to initialize them (if needed), and to initialize block and global expected scores
		//
		for (String sPath : _aFitScorePaths)
		{
			Widget widgetForPath = display.getControlForPath(sPath, null) ;
			if (null != widgetForPath)
			{
				if (widgetForPath instanceof FitScoreBase)
				{
					final FitScoreBase control = (FitScoreBase) widgetForPath ;

					// Is this control involved in the global score?
					//
					boolean bValidForGlobal = ("".equals(sTargetRoot) || sPath.startsWith(sTargetRoot)) ;

					// Initialize the control, if not already set
					//
					control.initIfUnset(_sTraineeSeniority) ;

					// Get control's expected score (for controls with a "not testable" option, the expected score is 0 when this option is selected)
					//
					double dExpected = control.getExpectedContentAsDouble(_sTraineeSeniority) ;

					// Increment global score
					//
					if (bValidForGlobal)
						_dExpectedWeightsCount += dExpected ;

					// Look for a block score to increment
					//
					GlobalExpectedScoreControl blockExpectedScore = getBlockExpectedScore(control.getControlBase()) ;
					if (null != blockExpectedScore)
					{
						// If this block score has not already been encountered, we reset its content
						//
						String sScorePath = blockExpectedScore.getControlBase().getPath() ;
						if (false == aExpectedBlocsScores.contains(sScorePath))
						{
							blockExpectedScore.setContent(null, "") ;
							aExpectedBlocsScores.add(sScorePath) ;
						}

						// Increment content
						//
						blockExpectedScore.addToContent(dExpected) ;
					}
				}
			}
		}

		// Adjust so the score is "base 100"
		//
		if (null != expectedScore)
		{
			// dScore = getWeightedScore(dScore) ;
			expectedScore.setValue(_dExpectedWeightsCount) ;
		}
	}

	/**
	 * Get the expected value from a path as a double 
	 * 
	 * */
	protected double getDoubleExpectedValueForPath(final String sPath)
	{
		if ((null == sPath) || "".equals(sPath))
			return -1 ;

		// Get the information entered in form
		//
		// FormDataData formData = display.getContentForPath(sPath) ;

		String sValue = getValueForPath(sPath, null) ;

		if ("".equals(sValue))
			return -1 ;

		try {
			return Double.parseDouble(sValue) ;
		} catch (NumberFormatException e) {
			return -1 ;
		}
	}

	/**
	 * Get the block global score display control for the block a given control belongs to
	 * 
	 * @param controlBase Base information for the control which global score display control is to be found 
	 * 
	 * @return The global score display control if found, <code>null</code> if not
	 */
	protected GlobalFitScoreControl getBlockGlobalScore(final ControlBase controlBase)
	{
		if (null == controlBase)
			return null ;

		// Get the "command block" that contains the caption for this branch and the controls block
		//
		FormBlockPanel commandBlock = getCommandBlockForControl(controlBase) ;
		if (null == commandBlock)
			return null ;

		// Get a GlobalFitScoreControl which direct father is the commandBlock
		//
		List<FormControl> aFormControls = display.getControls(null) ;
		if ((null == aFormControls) || aFormControls.isEmpty())
			return null ;

		for (FormControl formControl : aFormControls)
		{
			if (formControl.getControlBase().getFather() == commandBlock)
			{
				if (formControl.getWidget() instanceof GlobalFitScoreControl)
					return (GlobalFitScoreControl) formControl.getWidget() ; 
			}
		}

		return null ;
	}

	/**
	 * Get the block expected global score display control for the block a given control belongs to
	 * 
	 * @param controlBase Base information for the control which expected global score display control is to be found 
	 * 
	 * @return The expected global score display control if found, <code>null</code> if not
	 */
	protected GlobalExpectedScoreControl getBlockExpectedScore(final ControlBase controlBase)
	{
		if (null == controlBase)
			return null ;

		// Get the "command block" that contains the caption for this branch and the controls block
		//
		FormBlockPanel commandBlock = getCommandBlockForControl(controlBase) ;
		if (null == commandBlock)
			return null ;

		// Get a GlobalExpectedScoreControl which direct father is the commandBlock
		//
		List<FormControl> aFormControls = display.getControls(null) ;
		if ((null == aFormControls) || aFormControls.isEmpty())
			return null ;

		for (FormControl formControl : aFormControls)
		{
			if (formControl.getControlBase().getFather() == commandBlock)
			{
				if (formControl.getWidget() instanceof GlobalExpectedScoreControl)
					return (GlobalExpectedScoreControl) formControl.getWidget() ; 
			}
		}

		return null ;
	}

	/**
	 * Get the command block a given control belongs to
	 * 
	 * @param controlBase Base information for the given control
	 * 
	 * @return The block if found or <code>null</code> if not
	 */
	protected FormBlockPanel getCommandBlockForControl(final ControlBase controlBase)
	{
		if (null == controlBase)
			return null ;

		// Get the block all controls of the same branch are inserted into
		//
		FormBlockPanel controlsBlock = controlBase.getFather() ;
		if (null == controlsBlock)
			return null ;

		// Get the "command block" that contains the caption for this branch and the controls block
		//
		return controlsBlock.getFather() ;
	}

	/**
	 * Start process to reload information from the latest form for this trainee
	 * */
	public void initFromPreviousInformation()
	{
		int iTraineeId = getTraineeId() ;

		_dispatcher.execute(new GetCoachingFitPreviousFormBlockAction(_supervisor.getUserId(), iTraineeId, _iArchetypeId), new initFromFormCallback()) ;
	}

	/**
	 * The form was successfully saved, ask user if she wants to send it by mail before exiting
	 * 
	 * */
	public void leaveWhenSaved(int iFormId)
	{
		// If the display is already in screenshot mode, send the report by mail
		//
		if (display.isScreenShotMode())
		{
			// Don't save draft documents
			//
			if (false == _bSavingDraft)
				sendScreenshotByMail() ;

			_logger.info("Form saved, already in screen shot mode, leaving") ;

			eventBus.fireEvent(new GoToLoginResponseEvent()) ;

			return ;
		}

		if (_bSavingDraft)
		{
			_logger.info("Form saved in draft mode, leaving") ;

			eventBus.fireEvent(new GoToLoginResponseEvent()) ;

			return ;
		}

		_logger.info("Form saved, re-opening in screen shot mode") ;

		// Reset display
		//
		display.setDefaultValues() ;

		// Send an event to open the view in "screenshot mode"
		//
		int iScreenShotAnnotationId = -1 ;
		if (iFormId != _iFormId)
			iScreenShotAnnotationId = iFormId ;

		eventBus.fireEvent(new EditFormEvent(_encounterSpace, _iFormId, -1, null, true, iScreenShotAnnotationId)) ;
	}

	/**
	 * Get the screenshot and send it by mail
	 */
	protected void sendScreenshotByMail()
	{
		String sHTMLcontent = display.getWorkspaceHtmlContent() ;
		if ((null != sHTMLcontent) && (false == "".equals(sHTMLcontent)))
		{
			sHTMLcontent = rectifyHtml(sHTMLcontent) ;

			sendByMail(sHTMLcontent) ;
		}
	}

	/**
	 * Bad hack.<br>
	 * The TextArea component returns CR LF as <code>\r\n</code> ; we need to transform them as <code>&lt;br&gt;</code><br>
	 * We cannot do it earlier since the <code>panel.toString()</code> method would get the <code>&lt;br&gt;</code> as <code>lt br gt<code><br>
	 */
	protected String rectifyHtml(final String sHTMLcontent)
	{
		if ((null == sHTMLcontent) || "".equals(sHTMLcontent))
			return sHTMLcontent ;

		// String sReturn = sHTMLcontent.replaceAll("&lt;br /&gt;", "<br />") ;
		String sReturn = sHTMLcontent.replaceAll("(\r\n|\n)", "<br />") ;

		return sReturn ; 
	}

	/**
	 * Ask the server to send the mail, with its body and attached content
	 */
	protected void sendByMail(final String sHTMLcontent)
	{
		List<MailTo> aMailAddresses = new ArrayList<MailTo>() ;

		// In the mail addresses list, change trainee and coach by their true mail addresses
		// Also create a MailTo object for each variable
		//
		for (MailTo mailTo : _aMailAddresses)
		{
			String sVars = mailTo.getAddress() ;

			String[] aVars = sVars.split(";") ;
			int iVarsSize = aVars.length ;
			for (int i = 0 ; i < iVarsSize ; i++)
			{
				String sVar = aVars[i] ;

				if ("$trainee$".equalsIgnoreCase(sVar))
				{
					// Get trainee's mail address
					//
					if (_iLastSavedTrainee > 0)  // at this point, the trainee is no longer available in view
					{
						CoachingFitUser user = (CoachingFitUser) _supervisor.getUser() ;
						TraineeData traineeInformation = user.getTraineeFromId(_iLastSavedTrainee) ;
						String sMailForTrainee = traineeInformation.getEMail() ;
						if (false == "".equals(sMailForTrainee))
							aMailAddresses.add(new MailTo(sMailForTrainee, mailTo.getRecipientType())) ;
					}
				}
				else if ("$coach$".equalsIgnoreCase(sVar))
				{
					String sMailForUser = "" ;

					// Get coach's mail address
					//
					if (false == ((CoachingFitSupervisor) _supervisor).isUserATrainee())
					{
						CoachingFitUser user = (CoachingFitUser) _supervisor.getUser() ;					
						sMailForUser = user.getUserData().getEMail() ;
					}
					else
						sMailForUser = ((CoachingFitSupervisor) _supervisor).getTraineeUser().getEMail() ;

					if (false == "".equals(sMailForUser))
						aMailAddresses.add(new MailTo(sMailForUser, mailTo.getRecipientType())) ;
				}
				// Var that is neither the trainee nor the coach
				//
				else
					aMailAddresses.add(new MailTo(sVar, mailTo.getRecipientType())) ;
			}
		}

		_dispatcher.execute(new SendFormByMailAction(_supervisor.getUserId(), ((CoachingFitSupervisor) _supervisor).getTraineeAsUserId(), _iLastSavedTrainee, sHTMLcontent, _sMailBody, aMailAddresses, _sMailFrom, _sMailCaption), new sendFormByMailCallback()) ;
	}

	protected class sendFormByMailCallback implements AsyncCallback<SendFormByMailResult> 
	{
		public sendFormByMailCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "Unhandled error", cause) ;
		}//end handleFailure

		@Override
		public void onSuccess(SendFormByMailResult value) 
		{
			String sMessage = value.getMessage() ;
			if (false == "".equals(sMessage))
				display.popupWarningMessage("ERROR_REPORT_NOT_SENT|" + sMessage) ;
		}
	}

	/**
	 * Transform the mail body template into a genuine mail body by replacing variables by their instantiated values
	 * 
	 * @return The mail body, can be <code>""</code>
	 */
	protected String buildMailBody()
	{
		if ((null == _sMailTemplate) || "".equals(_sMailTemplate))
			return "" ;

		String sInstantiated = _sMailTemplate ; 

		int iTagPos = sInstantiated.indexOf("[VALUE") ;
		while (iTagPos >= 0)
		{
			int iBodyLen = sInstantiated.length() ; 

			// Get tag
			//
			int iPos = iTagPos + 6 ;

			int iEndTagPos = sInstantiated.indexOf("]", iPos) ;
			if (-1 == iEndTagPos)
				return sInstantiated ; 

			String sTag = sInstantiated.substring(iPos, iEndTagPos) ;
			sTag = MiscellanousFcts.strip(sTag, STRIP_DIRECTION.stripBoth, ' ') ;

			sInstantiated = sInstantiated.substring(0, iTagPos) + getTagValue(sTag) + sInstantiated.substring(iEndTagPos + 1, iBodyLen) ;
			iTagPos = sInstantiated.indexOf("[VALUE") ;
		}

		return sInstantiated ;
	}

	/**
	 * Get the text value for a tag (usually a path) 
	 */
	protected String getTagValue(final String sTag)
	{
		if ((null == sTag) || "".equals(sTag))
			return "" ;

		String[] aPath = sTag.split("\\/") ;

		int iPathSize = aPath.length ;

		// Trainee information
		//
		if ("$TR$".equals(aPath[0]))
		{
			int iTraineeId = getIntValueForPath("$trainee$", null) ;
			if (-1 == iTraineeId)
				return "" ;

			if (1 == iPathSize)
				return "" + iTraineeId ;

			// Get trainee
			//
			CoachingFitUser user = (CoachingFitUser) _supervisor.getUser() ;
			if (null == user)
				return "" ;

			TraineeData trainee = user.getTraineeFromId(iTraineeId) ;
			if ((null == trainee) || trainee.isEmpty())
				return "" ;

			if ("$label$".equals(aPath[1]))
				return trainee.getLabel() ;
			if ("$first$".equals(aPath[1]))
				return trainee.getFirstName() ;

			return "" ;
		}

		Widget widget = display.getControlForPath(sTag, null) ;		
		if (null == widget)
			return "" ;

		if ("FormTextArea".equals(widget.getClass().getSimpleName()))
		{
			FormTextArea textArea = (FormTextArea) widget ;
			String sValue = textArea.getContent().getValue() ;
			return sValue.replaceAll("(\r\n|\n)", "<br />") ;
		}
		if ("FormListBox".equals(widget.getClass().getSimpleName()))
		{
			FormListBox textListBox = (FormListBox) widget ;
			String sSelectedPath = textListBox.getContent().getPath() ;
			if ("".equals(sSelectedPath))
				return "" ;

			FormControlOptionData selectedOptionData = textListBox.getOptionForPath(sSelectedPath) ;
			if (null == selectedOptionData)
				return "" ;

			return selectedOptionData.getCaption() ;
		}
		if ("EventDateControl".equals(widget.getClass().getSimpleName()))
		{
			EventDateControl eventDate = (EventDateControl) widget ;
			String sDate = eventDate.getContent().getValue() ;

			CoachingFitDate date = new CoachingFitDate(sDate) ; 

			return "" + date.getDay() + " " + eventDate.getMonthLabel(date.getMonth()) + "" + date.getYear() ;
		}

		return "" ;
	}

	/**
	 * Get the region identifier a trainee belongs to
	 * 
	 * @return The region identifier if found, <code>-1</code> if not
	 * 
	 * */
	protected int getRegionForTrainee(final int iTraineeId)
	{
		CoachingFitUser user = ((CoachingFitSupervisor) _supervisor).getCoachingFitUser() ;
		if (null == user)
			return -1 ;

		TraineeData traineeData = user.getTraineeFromId(iTraineeId) ;
		if (null == traineeData)
			return -1 ;

		return traineeData.getRegionId() ;
	}

	protected class initFromFormCallback implements AsyncCallback<GetCoachingFitPreviousFormBlockResult> 
	{
		public initFromFormCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "Unhandled error for initFromForm", cause) ;

		}//end handleFailure

		@Override
		public void onSuccess(GetCoachingFitPreviousFormBlockResult value) 
		{
			// No previous information found
			//
			if (null == value.getFormBlock())
				return ;

			FormBlock<FormDataData> previousBlock = new FormBlock<FormDataData>(value.getFormBlock()) ;
			initControlsFromPreviousInformation((FormBlockModel<FormDataData>) previousBlock) ;
		}
	}

	/**
	 * Get trainee information from server and store them in user's library
	 * 
	 * @param iTraineeId identifier of trainee whose information is to look for
	 * 
	 * */
	protected void getTraineeInformation(final int iTraineeId)
	{
		_dispatcher.execute(new GetCoachingFitTraineeAction(_supervisor.getUserId(), iTraineeId), new getTraineeInformationCallback()) ;
	}

	protected class getTraineeInformationCallback implements AsyncCallback<GetCoachingFitTraineeResult> 
	{
		public getTraineeInformationCallback() {
			super() ;
		}

		@Override
		public void onFailure(Throwable cause) {
			_logger.log(Level.SEVERE, "Unhandled error for getTraineeInformation", cause) ;
		}//end handleFailure

		@Override
		public void onSuccess(GetCoachingFitTraineeResult value) 
		{
			addTraineeToUserLibrary(value.getTraineeData()) ;

			getArchetype() ;
		}
	}

	protected void addTraineeToUserLibrary(final TraineeData traineeData)
	{
		if (null == traineeData)
			return ;

		CoachingFitUser user = (CoachingFitUser) _supervisor.getUser() ;
		if (null == user)
			return ;

		user.addTrainee(traineeData) ;
	}

	/**
	 * Get trainee identifier from trainee selection interface element in the view
	 * 
	 * */
	protected int getTraineeId()
	{
		// Get trainee Id
		//
		FormDataData traineeData = display.getContentForPath("$trainee$", null) ;
		if (null == traineeData)
			return 0 ;

		String sValue = traineeData.getValue() ;
		if ("".equals(sValue))
			return 0 ;

		int iTraineeId = 0 ;

		try {
			iTraineeId = Integer.parseInt(sValue) ;
		} catch (NumberFormatException e) {
			return 0 ;
		}

		return iTraineeId ;
	}

	/**
	 * Function called after the form has been displayed 
	 * 
	 * */
	public void executePostDisplayProcesses() 
	{
		if (display.isScreenShotMode())
		{
			if (false == _bSavingDraft)
			{
				_logger.info("Sending screen shot by mail.") ;
				sendScreenshotByMail() ;
			}
			else
				_logger.info("Displayed in screen shot mode, but don't send by mail.") ;

			eventBus.fireEvent(new GoToLoginResponseEvent()) ;
		}
		else
			fixTraineesList() ;
	}

	@Override
	protected void onRevealDisplay()
	{
		// TODO Auto-generated method stub
	}	
}
