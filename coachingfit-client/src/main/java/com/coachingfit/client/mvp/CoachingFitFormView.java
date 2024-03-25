package com.coachingfit.client.mvp;

import java.util.ArrayList;
import java.util.List;

import com.coachingfit.client.global.CoachingFitSupervisor;
import com.coachingfit.client.loc.CoachingFitConstants;
import com.coachingfit.client.widgets.DisplaySeniorityControl;
import com.coachingfit.client.widgets.FitScoreControl;
import com.coachingfit.client.widgets.FitScorePlusControl;
import com.coachingfit.client.widgets.GlobalExpectedScoreControl;
import com.coachingfit.client.widgets.GlobalFitScoreControl;
import com.coachingfit.client.widgets.SelectTraineeControl;
import com.coachingfit.client.widgets.SetScoreControl;
import com.coachingfit.client.widgets.TenScoreControl;
import com.coachingfit.client.widgets.TenScorePlusControl;
import com.coachingfit.client.widgets.VscScoreControl;
import com.coachingfit.shared.database.TraineeData;
import com.coachingfit.shared.model.CoachingFitUser;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import com.google.inject.Inject;

import com.primege.client.mvp.FormViewModel;
import com.primege.client.util.FormControl;
import com.primege.client.util.FormControlOptionData;
import com.primege.client.widgets.FormBlockPanel;
import com.primege.shared.database.FormDataData;

public class CoachingFitFormView extends FormViewModel implements CoachingFitFormPresenter.Display
{	
	private List<TraineeData>                _aTrainees ;

	private GlobalFitScoreControl            _fitTotal ;
	private GlobalExpectedScoreControl       _fitTotalExpected ;
	private DisplaySeniorityControl          _seniority ;

	private List<GlobalFitScoreControl>      _aBlockTotalScores    = new ArrayList<GlobalFitScoreControl>() ;
	private List<GlobalExpectedScoreControl> _aBlockExpectedScores = new ArrayList<GlobalExpectedScoreControl>() ;

	private String                           _sSeniorityForScreenShot ;

	private final CoachingFitConstants localConstants = GWT.create(CoachingFitConstants.class) ;

	@Inject
	public CoachingFitFormView(final CoachingFitSupervisor supervisor)
	{
		super(supervisor) ;

		_fitTotal         = null ;
		_fitTotalExpected = null ;
		_seniority        = null ;

		_sSeniorityForScreenShot = "" ;
	}

	/** 
	 * Reset everything to display a new archetype
	 */
	@Override
	public void resetAll()
	{
		super.resetAll() ;

		_fitTotal         = null ;
		_fitTotalExpected = null ;
		_seniority        = null ;

		_aBlockTotalScores.clear() ;
		_aBlockExpectedScores.clear() ;

		_sSeniorityForScreenShot = "" ;
	}

	/** 
	 * Insert a new control to the form
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
	 * @param bInPdfWhenEmpty <code>true</code> if the control is to be inserted in the PDF, even when empty, <code>false</code> if not
	 * @param masterBlock     Form's root panel (if null, then the form global panel is used)
	 * @param bEdited         <code>true</code> if the form is being edited, <code>false</code> if it is new
	 */
	@Override
	public void insertNewControl(final String sControlPath, final List<FormDataData> aContent, final String sControlCaption, final String sControlType, final String sControlSubtype, final String sControlUnit, final String sControlValue, final List<FormControlOptionData> aOptions, final String sControlStyle, final String sCaptionStyle, final boolean bInitFromPrev, final String sExclusion, final boolean bInBlockCell, final boolean bInPdfWhenEmpty, FormBlockPanel masterBlock, boolean bEdited)
	{
		FormBlockPanel referenceBlock = masterBlock ;
    if (null == referenceBlock)
      referenceBlock = _formPanel ;
		
		// Get the panel to add the control to
		// 
		FormBlockPanel currentBlock = getCurrentFormPanel(referenceBlock) ;
		if (null == currentBlock)
			return ;

		if ("Trainee".equalsIgnoreCase(sControlType))
		{
			FormDataData content = getSingleInformationContent(aContent) ;
			
			// Screenshot mode, just insert a Label that displays trainee's label
			//
			if (_bScreenShotMode)
			{
				if ((null == content) || content.hasNoData())
				{
					if (bInPdfWhenEmpty)
						currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(""), null, bInBlockCell) ;
				}
				else
				{
					CoachingFitUser user = (CoachingFitUser) _supervisor.getUser() ;
					if (null != user)
					{
						String sTraineeId = content.getValue() ;
						if ((null != sTraineeId) && (false == "".equals(sTraineeId)))
						{
							int iTraineeId = Integer.parseInt(sTraineeId) ;
							TraineeData trainee = user.getTraineeFromId(iTraineeId) ;
							if (null != trainee)
							{
								currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(trainee.getLabel()), null, bInBlockCell) ;
								return ;
							}
						}
					}
					currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label("?"), null, bInBlockCell) ;
				}
				return ;
			}

			// Not in screenshot mode, insert a trainee selection control
			//

			// Sort the array
			//
			List<TraineeData> aSortedTrainees = CoachingFitSupervisor.getSortedTraineesArray(_aTrainees) ;

			SelectTraineeControl traineeControl = new SelectTraineeControl(aSortedTrainees, sControlPath) ;

			if ((null != sControlStyle) && false == "".equals(sControlStyle))
				traineeControl.addStyleName(sControlStyle) ;

			traineeControl.setContent(content, sControlValue) ;
			traineeControl.setInitFromPrev(bInitFromPrev) ;

			referenceBlock.addControl(new FormControl(traineeControl.getControlBase(), traineeControl, content, sExclusion)) ;
			currentBlock.insertControl(sControlCaption, sCaptionStyle, traineeControl, traineeControl.getControlBase(), bInBlockCell) ;
		}
		else if ("FitScore".equalsIgnoreCase(sControlType))
		{
			FormDataData content = getSingleInformationContent(aContent) ;
			
			// Screenshot mode, just insert a Label that displays the score
			//
			if (_bScreenShotMode)
			{
				if ((null == content) || content.hasNoData())
				{
					if (bInPdfWhenEmpty)
						currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(""), null, bInBlockCell) ;
				}
				else
				{
					FitScoreControl fitControl = new FitScoreControl(sControlPath) ;
					fitControl.setContent(content, sControlValue) ;

					if (false == "".equals(_sSeniorityForScreenShot))
						fitControl.initForScreenShotMode(_sSeniorityForScreenShot) ;

					currentBlock.insertControl(sControlCaption, fitControl.getStyle(), new Label(fitControl.getLabel()), null, bInBlockCell) ;
				}
				return ;
			}

			// Not in screenshot mode, insert a coaching fit scoring control
			//
			FitScoreControl fitControl = new FitScoreControl(sControlPath) ;

			if ((null != sControlStyle) && false == "".equals(sControlStyle))
				fitControl.addStyleName(sControlStyle) ;

			fitControl.setContent(content, sControlValue) ;
			fitControl.setInitFromPrev(bInitFromPrev) ;

			referenceBlock.addControl(new FormControl(fitControl.getControlBase(), fitControl, content, sExclusion)) ;
			currentBlock.insertControl(sControlCaption, sCaptionStyle, fitControl, fitControl.getControlBase(), bInBlockCell) ;
		}
		else if ("FitScorePlus".equalsIgnoreCase(sControlType))
		{
			FormDataData content = getSingleInformationContent(aContent) ;
			
			// Screenshot mode, just insert a Label that displays the score
			//
			if (_bScreenShotMode)
			{
				if ((null == content) || content.hasNoData())
				{
					if (bInPdfWhenEmpty)
						currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(""), null, bInBlockCell) ;
				}
				else
				{
					FitScorePlusControl fitPlusControl = new FitScorePlusControl(sControlPath) ;
					fitPlusControl.setContent(content, sControlValue) ;

					if (false == "".equals(_sSeniorityForScreenShot))
						fitPlusControl.initForScreenShotMode(_sSeniorityForScreenShot) ;

					currentBlock.insertControl(sControlCaption, fitPlusControl.getStyle(), new Label(fitPlusControl.getLabel()), null, bInBlockCell) ;
				}
				return ;
			}

			// Not in screenshot mode, insert a coaching fit scoring control
			//
			FitScorePlusControl fitPlusControl = new FitScorePlusControl(sControlPath) ;

			if ((null != sControlStyle) && false == "".equals(sControlStyle))
				fitPlusControl.addStyleName(sControlStyle) ;

			fitPlusControl.setContent(content, sControlValue) ;
			fitPlusControl.setInitFromPrev(bInitFromPrev) ;

			referenceBlock.addControl(new FormControl(fitPlusControl.getControlBase(), fitPlusControl, content, sExclusion)) ;
			currentBlock.insertControl(sControlCaption, sCaptionStyle, fitPlusControl, fitPlusControl.getControlBase(), bInBlockCell) ;
		}
		else if ("SetScore".equalsIgnoreCase(sControlType))
		{
			FormDataData content = getSingleInformationContent(aContent) ;
			
			// Screenshot mode, just insert a Label that displays the score
			//
			if (_bScreenShotMode)
			{
				if ((null == content) || content.hasNoData())
				{
					if (bInPdfWhenEmpty)
						currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(""), null, bInBlockCell) ;
				}
				else
				{
					SetScoreControl setControl = new SetScoreControl(sControlPath) ;
					setControl.setContent(content, sControlValue) ;

					if (false == "".equals(_sSeniorityForScreenShot))
						setControl.initForScreenShotMode(_sSeniorityForScreenShot) ;

					currentBlock.insertControl(sControlCaption, setControl.getStyle(), new Label(setControl.getLabel()), null, bInBlockCell) ;
				}
				return ;
			}

			// Not in screenshot mode, insert a coaching fit scoring control
			//
			SetScoreControl setControl = new SetScoreControl(sControlPath) ;

			if ((null != sControlStyle) && false == "".equals(sControlStyle))
				setControl.addStyleName(sControlStyle) ;

			setControl.setContent(content, sControlValue) ;
			setControl.setInitFromPrev(bInitFromPrev) ;

			referenceBlock.addControl(new FormControl(setControl.getControlBase(), setControl, content, sExclusion)) ;
			currentBlock.insertControl(sControlCaption, sCaptionStyle, setControl, setControl.getControlBase(), bInBlockCell) ;
		}
		else if ("SplfVsc".equalsIgnoreCase(sControlType))
		{
			FormDataData content = getSingleInformationContent(aContent) ;
			
			// Screenshot mode, just insert a Label that displays the score
			//
			if (_bScreenShotMode)
			{
				if ((null == content) || content.hasNoData())
				{
					if (bInPdfWhenEmpty)
						currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(""), null, bInBlockCell) ;
				}
				else
				{
					VscScoreControl vscControl = new VscScoreControl(sControlPath) ;
					vscControl.setContent(content, sControlValue) ;

					if (false == "".equals(_sSeniorityForScreenShot))
						vscControl.initForScreenShotMode(_sSeniorityForScreenShot) ;

					currentBlock.insertControl(sControlCaption, vscControl.getStyle(), new Label(vscControl.getLabel()), null, bInBlockCell) ;
				}
				return ;
			}

			// Not in screenshot mode, insert a coaching fit scoring control
			//
			VscScoreControl vscControl = new VscScoreControl(sControlPath) ;

			if ((null != sControlStyle) && false == "".equals(sControlStyle))
				vscControl.addStyleName(sControlStyle) ;

			vscControl.setContent(content, sControlValue) ;
			vscControl.setInitFromPrev(bInitFromPrev) ;

			referenceBlock.addControl(new FormControl(vscControl.getControlBase(), vscControl, content, sExclusion)) ;
			currentBlock.insertControl(sControlCaption, sCaptionStyle, vscControl, vscControl.getControlBase(), bInBlockCell) ;
		}
		else if ("TenScore".equalsIgnoreCase(sControlType))
		{
			FormDataData content = getSingleInformationContent(aContent) ;
			
			// Screenshot mode, just insert a Label that displays the score
			//
			if (_bScreenShotMode)
			{
				if ((null == content) || content.hasNoData())
				{
					if (bInPdfWhenEmpty)
						currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(""), null, bInBlockCell) ;
				}
				else
				{
					TenScoreControl tenScoreControl = new TenScoreControl(sControlPath) ;
					tenScoreControl.setContent(content, sControlValue) ;

					if (false == "".equals(_sSeniorityForScreenShot))
						tenScoreControl.initForScreenShotMode(_sSeniorityForScreenShot) ;

					currentBlock.insertControl(sControlCaption, tenScoreControl.getStyle(), new Label(tenScoreControl.getLabel()), null, bInBlockCell) ;
				}
				return ;
			}

			// Not in screenshot mode, insert a coaching fit scoring control
			//
			TenScoreControl tenScoreControl = new TenScoreControl(sControlPath) ;

			if ((null != sControlStyle) && false == "".equals(sControlStyle))
				tenScoreControl.addStyleName(sControlStyle) ;

			tenScoreControl.setContent(content, sControlValue) ;
			tenScoreControl.setInitFromPrev(bInitFromPrev) ;

			referenceBlock.addControl(new FormControl(tenScoreControl.getControlBase(), tenScoreControl, content, sExclusion)) ;
			currentBlock.insertControl(sControlCaption, sCaptionStyle, tenScoreControl, tenScoreControl.getControlBase(), bInBlockCell) ;
		}
		else if ("TenScorePlus".equalsIgnoreCase(sControlType))
		{
			FormDataData content = getSingleInformationContent(aContent) ;
			
			// Screenshot mode, just insert a Label that displays the score
			//
			if (_bScreenShotMode)
			{
				if ((null == content) || content.hasNoData())
				{
					if (bInPdfWhenEmpty)
						currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(""), null, bInBlockCell) ;
				}
				else
				{
					TenScorePlusControl tenScorePlusControl = new TenScorePlusControl(sControlPath) ;
					tenScorePlusControl.setContent(content, sControlValue) ;

					if (false == "".equals(_sSeniorityForScreenShot))
						tenScorePlusControl.initForScreenShotMode(_sSeniorityForScreenShot) ;

					currentBlock.insertControl(sControlCaption, tenScorePlusControl.getStyle(), new Label(tenScorePlusControl.getLabel()), null, bInBlockCell) ;
				}
				return ;
			}

			// Not in screenshot mode, insert a coaching fit scoring control
			//
			TenScorePlusControl tenScorePlusControl = new TenScorePlusControl(sControlPath) ;

			if ((null != sControlStyle) && false == "".equals(sControlStyle))
				tenScorePlusControl.addStyleName(sControlStyle) ;

			tenScorePlusControl.setContent(content, sControlValue) ;
			tenScorePlusControl.setInitFromPrev(bInitFromPrev) ;

			referenceBlock.addControl(new FormControl(tenScorePlusControl.getControlBase(), tenScorePlusControl, content, sExclusion)) ;
			currentBlock.insertControl(sControlCaption, sCaptionStyle, tenScorePlusControl, tenScorePlusControl.getControlBase(), bInBlockCell) ;
		}
		else if ("FitTotal".equalsIgnoreCase(sControlType))
		{
			FormDataData content = getSingleInformationContent(aContent) ;
			
			// Screenshot mode, just insert a Label that displays the score
			//
			if (_bScreenShotMode)
			{
				if ((null == content) || content.hasNoData())
				{
					if (bInPdfWhenEmpty)
						currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(""), null, bInBlockCell) ;
				}
				else
					currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(content.getValue()), null, bInBlockCell) ;
				return ;
			}

			// Not in screenshot mode, insert a score display control
			//
			_fitTotal = new GlobalFitScoreControl(sControlPath) ;

			if ((null != sControlStyle) && false == "".equals(sControlStyle))
				_fitTotal.addStyleName(sControlStyle) ;

			_fitTotal.setContent(content, sControlValue) ;
			_fitTotal.setInitFromPrev(bInitFromPrev) ;

			referenceBlock.addControl(new FormControl(_fitTotal.getControlBase(), _fitTotal, content, sExclusion)) ;
			currentBlock.insertControl(sControlCaption, sCaptionStyle, _fitTotal, _fitTotal.getControlBase(), bInBlockCell) ;
		}
		else if ("FitTotalExpected".equalsIgnoreCase(sControlType))
		{
			FormDataData content = getSingleInformationContent(aContent) ;
			
			// Screenshot mode, just insert a Label that displays the score
			//
			if (_bScreenShotMode)
			{
				if ((null == content) || content.hasNoData())
				{
					if (bInPdfWhenEmpty)
						currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(""), null, bInBlockCell) ;
				}
				else
					currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(content.getValue()), null, bInBlockCell) ;
				return ;
			}

			// Not in screenshot mode, insert a score display control
			//
			_fitTotalExpected = new GlobalExpectedScoreControl(sControlPath) ;

			if ((null != sControlStyle) && false == "".equals(sControlStyle))
				_fitTotalExpected.addStyleName(sControlStyle) ;

			_fitTotalExpected.setContent(content, sControlValue) ;
			_fitTotalExpected.setInitFromPrev(bInitFromPrev) ;

			referenceBlock.addControl(new FormControl(_fitTotalExpected.getControlBase(), _fitTotalExpected, content, sExclusion)) ;
			currentBlock.insertControl(sControlCaption, sCaptionStyle, _fitTotalExpected, _fitTotalExpected.getControlBase(), bInBlockCell) ;
		}
		else if ("FitSubTotal".equalsIgnoreCase(sControlType))
		{
			FormDataData content = getSingleInformationContent(aContent) ;
			
			// Screenshot mode, just insert a Label that displays the score
			//
			if (_bScreenShotMode)
			{
				if ((null == content) || content.hasNoData())
				{
					if (bInPdfWhenEmpty)
						currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(""), null, bInBlockCell) ;
				}
				else
					currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(content.getValue()), null, bInBlockCell) ;
				return ;
			}

			// Not in screenshot mode, insert a score display control
			//
			GlobalFitScoreControl fitSubTotal = new GlobalFitScoreControl(sControlPath) ;

			if ((null != sControlStyle) && false == "".equals(sControlStyle))
				fitSubTotal.addStyleName(sControlStyle) ;

			fitSubTotal.setContent(content, sControlValue) ;
			fitSubTotal.setInitFromPrev(bInitFromPrev) ;

			referenceBlock.addControl(new FormControl(fitSubTotal.getControlBase(), fitSubTotal, content, sExclusion)) ;
			currentBlock.insertControl(sControlCaption, sCaptionStyle, fitSubTotal, fitSubTotal.getControlBase(), bInBlockCell) ;

			_aBlockTotalScores.add(fitSubTotal) ;
		}
		else if ("FitSubTotalExpected".equalsIgnoreCase(sControlType))
		{
			FormDataData content = getSingleInformationContent(aContent) ;
			
			// Screenshot mode, just insert a Label that displays the score
			//
			if (_bScreenShotMode)
			{
				if ((null == content) || content.hasNoData())
				{
					if (bInPdfWhenEmpty)
						currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(""), null, bInBlockCell) ;
				}
				else
					currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(content.getValue()), null, bInBlockCell) ;
				return ;
			}

			// Not in screenshot mode, insert a score display control
			//
			GlobalExpectedScoreControl fitSubTotalExpected = new GlobalExpectedScoreControl(sControlPath) ;

			if ((null != sControlStyle) && false == "".equals(sControlStyle))
				fitSubTotalExpected.addStyleName(sControlStyle) ;

			fitSubTotalExpected.setContent(content, sControlValue) ;
			fitSubTotalExpected.setInitFromPrev(bInitFromPrev) ;

			referenceBlock.addControl(new FormControl(fitSubTotalExpected.getControlBase(), fitSubTotalExpected, content, sExclusion)) ;
			currentBlock.insertControl(sControlCaption, sCaptionStyle, fitSubTotalExpected, fitSubTotalExpected.getControlBase(), bInBlockCell) ;

			_aBlockExpectedScores.add(fitSubTotalExpected) ;
		}
		else if ("Seniority".equalsIgnoreCase(sControlType))
		{
			FormDataData content = getSingleInformationContent(aContent) ;
			
			// Screenshot mode, just insert a Label that displays the seniority
			//
			if (_bScreenShotMode)
			{
				if ((null == content) || content.hasNoData())
				{
					if (bInPdfWhenEmpty)
						currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(""), null, bInBlockCell) ;
				}
				else
				{
					currentBlock.insertControl(sControlCaption, sCaptionStyle, new Label(DisplaySeniorityControl.getLabel(content.getValue(), localConstants.generalYear(), localConstants.generalYears(), localConstants.generalMonth(), localConstants.generalMonths())), null, bInBlockCell) ;
					_sSeniorityForScreenShot = content.getValue() ;
				}
				return ;
			}

			// Not in screenshot mode, insert a seniority display control
			//
			_seniority = new DisplaySeniorityControl(sControlPath) ;

			if ((null != sControlStyle) && false == "".equals(sControlStyle))
				_seniority.addStyleName(sControlStyle) ;

			_seniority.setContent(content, sControlValue) ;
			_seniority.setInitFromPrev(bInitFromPrev) ;

			referenceBlock.addControl(new FormControl(_seniority.getControlBase(), _seniority, content, sExclusion)) ;
			currentBlock.insertControl(sControlCaption, sCaptionStyle, _seniority, _seniority.getControlBase(), bInBlockCell) ;
		}
		else
			insertNewGenericControl(sControlPath, aContent, sControlCaption, sControlType, sControlSubtype, sControlUnit, sControlValue, aOptions, sControlStyle, sCaptionStyle, bInitFromPrev, sExclusion, bInBlockCell, bInPdfWhenEmpty, referenceBlock, bEdited) ;
	}
	
	@Override
	public HasChangeHandlers getTraineeChanged() 
	{
		Widget widget = getControlForPath("$trainee$", null) ;
		if (null == widget)
			return null ;

		SelectTraineeControl traineeControl = (SelectTraineeControl) widget ;
		return traineeControl ;
	}

	// Set/Update the list of trainees, and refresh the trainee selection control 
	//
	@Override
	public void setTrainees(List<TraineeData> trainees) 
	{
		_aTrainees = trainees ;

		Widget widget = getControlForPath("$trainee$", null) ;
		if (null == widget)
			return ;

		// Sort the array
		//
		List<TraineeData> aSortedTrainees = CoachingFitSupervisor.getSortedTraineesArray(trainees) ;

		SelectTraineeControl traineeControl = (SelectTraineeControl) widget ;
		traineeControl.updateTrainees(aSortedTrainees) ;
	}

	// Set/Update trainee's seniority information 
	//
	@Override
	public void setSeniority(final String sSeniority) {
		_seniority.setSeniority(sSeniority) ;
	}

	/**
	 * Return the total fit score control
	 */
	@Override
	public GlobalFitScoreControl getGlobalFitScoreControl() {
		return _fitTotal ;
	}

	/**
	 * Return the total expected score control
	 */
	@Override
	public GlobalExpectedScoreControl getGlobalExpectedScoreControl() {
		return _fitTotalExpected ;
	}

	/**
	 * Return the list of total scores for blocks
	 */
	@Override
	public List<GlobalFitScoreControl> getSubGlobalFitScoreControls() {
		return _aBlockTotalScores ;
	}

	/**
	 * Return the list of expected total scores for blocks
	 */
	@Override
	public List<GlobalExpectedScoreControl> getSubExpectedFitScoreControls() {
		return _aBlockExpectedScores ;
	}

	@Override
	public String getWorkspaceHtmlContent()
	{
		if (null == _formPanel)
			return "" ;

		return _formPanel.toString() ;
	}

	/** 
	 * Get message text from message ID
	 * 
	 * @param    sMessage the unique identifier of the message
	 * @return   the human readable message
	 */
	protected String getPopupWarningMessage(final String sMessage)
	{
		if ((null == sMessage) || "".equals(sMessage))
			return "" ;

		String sCode = sMessage ;
		String sMore = "" ;

		int iSeparator = sMessage.indexOf("|") ;

		// The message starts with a '|', means that there is no code
		//
		if (0 == iSeparator)
		{
			sMore = sCode.substring(1) ;
			sCode = "" ;
		}
		else if (iSeparator > 0)
		{
			sMore = sCode.substring(iSeparator + 1) ;
			sCode = sCode.substring(0, iSeparator) ;
		}

		String sReturn = "" ;

		if (false == "".equals(sCode))
		{
			if      (sCode.equals("WARNING_FORM_ALREADY_EXIST"))
				sReturn = localConstants.warningAlreadyExist() ;
			else if (sCode.equals("ERROR_MUST_ENTER_EVERY_INFORMATION"))
				sReturn = localConstants.mandatoryEnterAll() ;
			else if (sCode.equals("ERROR_REPORT_NOT_SENT"))
				sReturn = localConstants.couldNotSendReportByMail() ;

			if ((false == "".equals(sReturn)) && (false == "".equals(sMore)))
				sReturn += " (" + sMore + ")" ;
		}
		else
			sReturn = sMore ;

		return sReturn ;
	}

	@Override
	public Widget asWidget() {
		return this;
	}
}
