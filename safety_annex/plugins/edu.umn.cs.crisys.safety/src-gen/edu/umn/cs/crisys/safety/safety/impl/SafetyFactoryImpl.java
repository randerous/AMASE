/**
 */
package edu.umn.cs.crisys.safety.safety.impl;

import edu.umn.cs.crisys.safety.safety.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class SafetyFactoryImpl extends EFactoryImpl implements SafetyFactory
{
  /**
   * Creates the default factory implementation.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public static SafetyFactory init()
  {
    try
    {
      SafetyFactory theSafetyFactory = (SafetyFactory)EPackage.Registry.INSTANCE.getEFactory(SafetyPackage.eNS_URI);
      if (theSafetyFactory != null)
      {
        return theSafetyFactory;
      }
    }
    catch (Exception exception)
    {
      EcorePlugin.INSTANCE.log(exception);
    }
    return new SafetyFactoryImpl();
  }

  /**
   * Creates an instance of the factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SafetyFactoryImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EObject create(EClass eClass)
  {
    switch (eClass.getClassifierID())
    {
      case SafetyPackage.ANNEX_LIBRARY: return createAnnexLibrary();
      case SafetyPackage.ANNEX_SUBCLAUSE: return createAnnexSubclause();
      case SafetyPackage.SAFETY_LIBRARY: return createSafetyLibrary();
      case SafetyPackage.SAFETY_SUBCLAUSE: return createSafetySubclause();
      case SafetyPackage.CONTRACT: return createContract();
      case SafetyPackage.SPEC_STATEMENT: return createSpecStatement();
      case SafetyPackage.ANALYSIS_BEHAVIOR: return createAnalysisBehavior();
      case SafetyPackage.FAULT_SUBCOMPONENT: return createFaultSubcomponent();
      case SafetyPackage.HW_FAULT_SUBCOMPONENT: return createHWFaultSubcomponent();
      case SafetyPackage.PROPAGATION_TYPE_CONSTRAINT: return createPropagationTypeConstraint();
      case SafetyPackage.TEMPORAL_CONSTRAINT: return createTemporalConstraint();
      case SafetyPackage.TRIGGER_CONDITION: return createTriggerCondition();
      case SafetyPackage.SAFETY_EQ_STATEMENT: return createSafetyEqStatement();
      case SafetyPackage.INTERVAL: return createInterval();
      case SafetyPackage.SAFETY_CONTRACT_LIBRARY: return createSafetyContractLibrary();
      case SafetyPackage.SAFETY_CONTRACT_SUBCLAUSE: return createSafetyContractSubclause();
      case SafetyPackage.SAFETY_CONTRACT: return createSafetyContract();
      case SafetyPackage.FAULT_STATEMENT: return createFaultStatement();
      case SafetyPackage.ANALYSIS_STATEMENT: return createAnalysisStatement();
      case SafetyPackage.HW_FAULT_STATEMENT: return createHWFaultStatement();
      case SafetyPackage.PROPAGATE_STATEMENT: return createPropagateStatement();
      case SafetyPackage.ACTIVATION_STATEMENT: return createActivationStatement();
      case SafetyPackage.FAULT_COUNT_BEHAVIOR: return createFaultCountBehavior();
      case SafetyPackage.PROBABILITY_BEHAVIOR: return createProbabilityBehavior();
      case SafetyPackage.INPUT_STATEMENT: return createInputStatement();
      case SafetyPackage.OUTPUT_STATEMENT: return createOutputStatement();
      case SafetyPackage.DURATION_STATEMENT: return createDurationStatement();
      case SafetyPackage.PROBABILITY_STATEMENT: return createProbabilityStatement();
      case SafetyPackage.TRIGGER_STATEMENT: return createTriggerStatement();
      case SafetyPackage.PROPAGATION_TYPE_STATEMENT: return createPropagationTypeStatement();
      case SafetyPackage.ASYMMETRIC: return createasymmetric();
      case SafetyPackage.SYMMETRIC: return createsymmetric();
      case SafetyPackage.PERMANENT_CONSTRAINT: return createPermanentConstraint();
      case SafetyPackage.TRANSIENT_CONSTRAINT: return createTransientConstraint();
      case SafetyPackage.ENABLER_CONDITION: return createEnablerCondition();
      case SafetyPackage.EQ_VALUE: return createEqValue();
      case SafetyPackage.INTERVAL_EQ: return createIntervalEq();
      case SafetyPackage.SET_EQ: return createSetEq();
      case SafetyPackage.RANGE_EQ: return createRangeEq();
      case SafetyPackage.CLOSED_INTERVAL: return createClosedInterval();
      case SafetyPackage.OPEN_LEFT_INTERVAL: return createOpenLeftInterval();
      case SafetyPackage.OPEN_RIGHT_INTERVAL: return createOpenRightInterval();
      case SafetyPackage.OPEN_INTERVAL: return createOpenInterval();
      default:
        throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
    }
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public AnnexLibrary createAnnexLibrary()
  {
    AnnexLibraryImpl annexLibrary = new AnnexLibraryImpl();
    return annexLibrary;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public AnnexSubclause createAnnexSubclause()
  {
    AnnexSubclauseImpl annexSubclause = new AnnexSubclauseImpl();
    return annexSubclause;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SafetyLibrary createSafetyLibrary()
  {
    SafetyLibraryImpl safetyLibrary = new SafetyLibraryImpl();
    return safetyLibrary;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SafetySubclause createSafetySubclause()
  {
    SafetySubclauseImpl safetySubclause = new SafetySubclauseImpl();
    return safetySubclause;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Contract createContract()
  {
    ContractImpl contract = new ContractImpl();
    return contract;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SpecStatement createSpecStatement()
  {
    SpecStatementImpl specStatement = new SpecStatementImpl();
    return specStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public AnalysisBehavior createAnalysisBehavior()
  {
    AnalysisBehaviorImpl analysisBehavior = new AnalysisBehaviorImpl();
    return analysisBehavior;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FaultSubcomponent createFaultSubcomponent()
  {
    FaultSubcomponentImpl faultSubcomponent = new FaultSubcomponentImpl();
    return faultSubcomponent;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public HWFaultSubcomponent createHWFaultSubcomponent()
  {
    HWFaultSubcomponentImpl hwFaultSubcomponent = new HWFaultSubcomponentImpl();
    return hwFaultSubcomponent;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public PropagationTypeConstraint createPropagationTypeConstraint()
  {
    PropagationTypeConstraintImpl propagationTypeConstraint = new PropagationTypeConstraintImpl();
    return propagationTypeConstraint;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TemporalConstraint createTemporalConstraint()
  {
    TemporalConstraintImpl temporalConstraint = new TemporalConstraintImpl();
    return temporalConstraint;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TriggerCondition createTriggerCondition()
  {
    TriggerConditionImpl triggerCondition = new TriggerConditionImpl();
    return triggerCondition;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SafetyEqStatement createSafetyEqStatement()
  {
    SafetyEqStatementImpl safetyEqStatement = new SafetyEqStatementImpl();
    return safetyEqStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Interval createInterval()
  {
    IntervalImpl interval = new IntervalImpl();
    return interval;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SafetyContractLibrary createSafetyContractLibrary()
  {
    SafetyContractLibraryImpl safetyContractLibrary = new SafetyContractLibraryImpl();
    return safetyContractLibrary;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SafetyContractSubclause createSafetyContractSubclause()
  {
    SafetyContractSubclauseImpl safetyContractSubclause = new SafetyContractSubclauseImpl();
    return safetyContractSubclause;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SafetyContract createSafetyContract()
  {
    SafetyContractImpl safetyContract = new SafetyContractImpl();
    return safetyContract;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FaultStatement createFaultStatement()
  {
    FaultStatementImpl faultStatement = new FaultStatementImpl();
    return faultStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public AnalysisStatement createAnalysisStatement()
  {
    AnalysisStatementImpl analysisStatement = new AnalysisStatementImpl();
    return analysisStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public HWFaultStatement createHWFaultStatement()
  {
    HWFaultStatementImpl hwFaultStatement = new HWFaultStatementImpl();
    return hwFaultStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public PropagateStatement createPropagateStatement()
  {
    PropagateStatementImpl propagateStatement = new PropagateStatementImpl();
    return propagateStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ActivationStatement createActivationStatement()
  {
    ActivationStatementImpl activationStatement = new ActivationStatementImpl();
    return activationStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public FaultCountBehavior createFaultCountBehavior()
  {
    FaultCountBehaviorImpl faultCountBehavior = new FaultCountBehaviorImpl();
    return faultCountBehavior;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ProbabilityBehavior createProbabilityBehavior()
  {
    ProbabilityBehaviorImpl probabilityBehavior = new ProbabilityBehaviorImpl();
    return probabilityBehavior;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public InputStatement createInputStatement()
  {
    InputStatementImpl inputStatement = new InputStatementImpl();
    return inputStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OutputStatement createOutputStatement()
  {
    OutputStatementImpl outputStatement = new OutputStatementImpl();
    return outputStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public DurationStatement createDurationStatement()
  {
    DurationStatementImpl durationStatement = new DurationStatementImpl();
    return durationStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ProbabilityStatement createProbabilityStatement()
  {
    ProbabilityStatementImpl probabilityStatement = new ProbabilityStatementImpl();
    return probabilityStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TriggerStatement createTriggerStatement()
  {
    TriggerStatementImpl triggerStatement = new TriggerStatementImpl();
    return triggerStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public PropagationTypeStatement createPropagationTypeStatement()
  {
    PropagationTypeStatementImpl propagationTypeStatement = new PropagationTypeStatementImpl();
    return propagationTypeStatement;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public asymmetric createasymmetric()
  {
    asymmetricImpl asymmetric = new asymmetricImpl();
    return asymmetric;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public symmetric createsymmetric()
  {
    symmetricImpl symmetric = new symmetricImpl();
    return symmetric;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public PermanentConstraint createPermanentConstraint()
  {
    PermanentConstraintImpl permanentConstraint = new PermanentConstraintImpl();
    return permanentConstraint;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public TransientConstraint createTransientConstraint()
  {
    TransientConstraintImpl transientConstraint = new TransientConstraintImpl();
    return transientConstraint;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EnablerCondition createEnablerCondition()
  {
    EnablerConditionImpl enablerCondition = new EnablerConditionImpl();
    return enablerCondition;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EqValue createEqValue()
  {
    EqValueImpl eqValue = new EqValueImpl();
    return eqValue;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public IntervalEq createIntervalEq()
  {
    IntervalEqImpl intervalEq = new IntervalEqImpl();
    return intervalEq;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SetEq createSetEq()
  {
    SetEqImpl setEq = new SetEqImpl();
    return setEq;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public RangeEq createRangeEq()
  {
    RangeEqImpl rangeEq = new RangeEqImpl();
    return rangeEq;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ClosedInterval createClosedInterval()
  {
    ClosedIntervalImpl closedInterval = new ClosedIntervalImpl();
    return closedInterval;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpenLeftInterval createOpenLeftInterval()
  {
    OpenLeftIntervalImpl openLeftInterval = new OpenLeftIntervalImpl();
    return openLeftInterval;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpenRightInterval createOpenRightInterval()
  {
    OpenRightIntervalImpl openRightInterval = new OpenRightIntervalImpl();
    return openRightInterval;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public OpenInterval createOpenInterval()
  {
    OpenIntervalImpl openInterval = new OpenIntervalImpl();
    return openInterval;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public SafetyPackage getSafetyPackage()
  {
    return (SafetyPackage)getEPackage();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @deprecated
   * @generated
   */
  @Deprecated
  public static SafetyPackage getPackage()
  {
    return SafetyPackage.eINSTANCE;
  }

} //SafetyFactoryImpl
