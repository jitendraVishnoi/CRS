/*<ORACLECOPYRIGHT>
 * Copyright (C) 1994-2014 Oracle and/or its affiliates. All rights reserved.
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.
 * UNIX is a registered trademark of The Open Group.
 *
 * This software and related documentation are provided under a license agreement 
 * containing restrictions on use and disclosure and are protected by intellectual property laws. 
 * Except as expressly permitted in your license agreement or allowed by law, you may not use, copy, 
 * reproduce, translate, broadcast, modify, license, transmit, distribute, exhibit, perform, publish, 
 * or display any part, in any form, or by any means. Reverse engineering, disassembly, 
 * or decompilation of this software, unless required by law for interoperability, is prohibited.
 *
 * The information contained herein is subject to change without notice and is not warranted to be error-free. 
 * If you find any errors, please report them to us in writing.
 *
 * U.S. GOVERNMENT RIGHTS Programs, software, databases, and related documentation and technical data delivered to U.S. 
 * Government customers are "commercial computer software" or "commercial technical data" pursuant to the applicable 
 * Federal Acquisition Regulation and agency-specific supplemental regulations. 
 * As such, the use, duplication, disclosure, modification, and adaptation shall be subject to the restrictions and 
 * license terms set forth in the applicable Government contract, and, to the extent applicable by the terms of the 
 * Government contract, the additional rights set forth in FAR 52.227-19, Commercial Computer Software License 
 * (December 2007). Oracle America, Inc., 500 Oracle Parkway, Redwood City, CA 94065.
 *
 * This software or hardware is developed for general use in a variety of information management applications. 
 * It is not developed or intended for use in any inherently dangerous applications, including applications that 
 * may create a risk of personal injury. If you use this software or hardware in dangerous applications, 
 * then you shall be responsible to take all appropriate fail-safe, backup, redundancy, 
 * and other measures to ensure its safe use. Oracle Corporation and its affiliates disclaim any liability for any 
 * damages caused by use of this software or hardware in dangerous applications.
 *
 * This software or hardware and documentation may provide access to or information on content, 
 * products, and services from third parties. Oracle Corporation and its affiliates are not responsible for and 
 * expressly disclaim all warranties of any kind with respect to third-party content, products, and services. 
 * Oracle Corporation and its affiliates will not be responsible for any loss, costs, 
 * or damages incurred due to your access to or use of third-party content, products, or services.
 </ORACLECOPYRIGHT>*/


package atg.epub;

import atg.dtm.TransactionDemarcation;
import atg.dtm.TransactionDemarcationException;
import atg.epub.project.Process;
import atg.epub.project.ProcessHome;
import atg.epub.project.ProjectConstants;
import atg.nucleus.GenericService;
import atg.process.action.ActionConstants;
import atg.process.action.ActionException;
import atg.repository.RepositoryItem;
import atg.security.Persona;
import atg.security.ThreadSecurityManager;
import atg.security.User;
import atg.userdirectory.UserDirectoryUserAuthority;
import atg.versionmanager.VersionManager;
import atg.versionmanager.WorkingContext;
import atg.versionmanager.Workspace;
import atg.versionmanager.exceptions.VersionException;
import atg.workflow.ActorAccessException;
import atg.workflow.MissingWorkflowDescriptionException;
import atg.workflow.WorkflowConstants;
import atg.workflow.WorkflowException;
import atg.workflow.WorkflowManager;
import atg.workflow.WorkflowView;

import javax.ejb.CreateException;
import javax.transaction.TransactionManager;

/**
 * This class is meant as a starting point for building a programatic import of data into a versioned
 * repository or file system. This class provides the shell code which creates a project and advances
 * the workflow. The /Content Administration/import.wdl and /Content Administration/import-staging.wdl
 * workflows are meant to be used with this code.
 *
 * In order to use this code, the importUserData() method should be overridden in a subclass. This is where
 * all the logic for importing the data should be inserted.
 *
 * Note that the logic in this class is designed to execute within a single transaction. If the amount of
 * data being imported is substantial, then it is the responsibility of the developer to implement transaction
 * batching in the importUserData() method.
 *
 * @author Manny Parasirakis
 * @version $Id: //product/Publishing/version/11.1/pws/sample-code/classes.jar/src/atg/epub/ProgramaticImportService.java#2 $
 */
public abstract class ProgramaticImportService extends GenericService {
  //-------------------------------------
  public static String CLASS_VERSION = "$Id: //product/Publishing/version/11.1/pws/sample-code/classes.jar/src/atg/epub/ProgramaticImportService.java#2 $$Change: 890096 $";

  //-------------------------------------
  /**
   * This abstract method is meant to be overridden in the user's subclass. This is where all the logic for
   * importing the user's data is to be done.
   */
  public abstract void importUserData(Process pProcess, TransactionDemarcation pTD) throws Exception;

  //-------------------------------------
  // property: transactionManager
  //-------------------------------------
  private TransactionManager mTransactionManager = null;

  /**
   * @return Returns the transactionManager.
   */
  public TransactionManager getTransactionManager() {
    return mTransactionManager;
  }

  /**
   * @param pTransactionManager The transactionManager to set.
   */
  public void setTransactionManager(TransactionManager pTransactionManager) {
    mTransactionManager = pTransactionManager;
  }

  //-------------------------------------
  // property: versionManager
  //-------------------------------------
  private VersionManager mVersionManager = null;

  /**
   * @return Returns the versionManager.
   */
  public VersionManager getVersionManager() {
    return mVersionManager;
  }

  /**
   * @param pVersionManager The versionManager to set.
   */
  public void setVersionManager(VersionManager pVersionManager) {
    mVersionManager = pVersionManager;
  }

  //-------------------------------------
  // property: workflowManager
  //-------------------------------------
  private WorkflowManager mWorkflowManager = null;

  /**
   * @return Returns the workflowManager.
   */
  public WorkflowManager getWorkflowManager() {
    return mWorkflowManager;
  }

  /**
   * @param pWorkflowManager The workflowManager to set.
   */
  public void setWorkflowManager(WorkflowManager pWorkflowManager) {
    mWorkflowManager = pWorkflowManager;
  }

  //-------------------------------------
  // property: userAuthority
  //-------------------------------------
  private UserDirectoryUserAuthority mUserAuthority = null;

  /**
   * Returns the UserAuthority
   */
  public UserDirectoryUserAuthority getUserAuthority() {
    return mUserAuthority;
  }

  /**
   * Sets the UserAuthority
   */
  public void setUserAuthority(UserDirectoryUserAuthority pUserAuthority) {
    mUserAuthority = pUserAuthority;
  }

  //-------------------------------------
  // property: personaPrefix
  //-------------------------------------
  private String mPersonaPrefix = "Profile$login$";

  /**
   * Returns the PersonaPrefix which is supplied for login
   */
  public String getPersonaPrefix() {
    return mPersonaPrefix;
  }

  /**
   * Sets the PersonaPrefix
   */
  public void setPersonaPrefix(String pPersonaPrefix) {
    mPersonaPrefix = pPersonaPrefix;
  }

  //-------------------------------------
  // property: userName
  //-------------------------------------
  private String mUserName = "publishing";

  /**
   * Returns the UserName which is supplied upon checkin and for logging in
   */
  public String getUserName() {
    return mUserName;
  }

  /**
   * Sets the UserName
   */
  public void setUserName(String pUserName) {
    mUserName = pUserName;
  }

  //-------------------------------------
  // property: workflowName
  //-------------------------------------
  private String mWorkflowName = "/Content Administration/import.wdl";

  /**
   * Returns the workflowName property
   */
  public String getWorkflowName() {
    return mWorkflowName;
  }

  /**
   * Sets the workflowName property
   */
  public void setWorkflowName(String string) {
    mWorkflowName = string;
  }

  //-------------------------------------
  // property: taskOutcomeId
  //-------------------------------------
  private String mTaskOutcomeId = "4.1.1";

  /**
   * @return Returns the taskOutcomeId.
   */
  public String getTaskOutcomeId() {
    return mTaskOutcomeId;
  }

  /**
   * @param pTaskOutcomeId The taskOutcomeId to set.
   */
  public void setTaskOutcomeId(String pTaskOutcomeId) {
    mTaskOutcomeId = pTaskOutcomeId;
  }

  //-------------------------------------
  // property: projectName
  //-------------------------------------
  private String mProjectName = "Content Administration Import";

  /**
   * @return Returns the projectName.
   */
  public String getProjectName() {
    return mProjectName;
  }

  /**
   * @param pProjectName The projectName to set.
   */
  public void setProjectName(String pProjectName) {
    mProjectName = pProjectName;
  }
  
  
  //-------------------------------------
  // property: activityId
  //-------------------------------------
  private String mActivityId;

  /**
   * @return Returns the activityId.
   */
  public String getActivityId() {
    return mActivityId;
  }

  /**
   * @param pActivityId The activityId to set.
   */
  public void setActivityId(String pActivityId) {
    mActivityId = pActivityId;
  }

  //-------------------------------------
  // Constructor
  //-------------------------------------
  public ProgramaticImportService() {
  }

  //-------------------------------------
  /**
   * This is the starting point for the service. In order to start it, the executeImport() method needs to
   * be called by another service. This method begins a transaction and sets the security
   * context on the thread for the user specified in the userName property. Next, it creates a project
   * and then calls importUserData(). Next, it attempts to advance the project's workflow. Finally, it
   * unsets the security context and commits the transaction.
   *
   * <b>NOTE!!! - This code only creates a single transaction and is suitable for imports which can fit
   * into the context of a single transaction. If you are doing large imports, then you must handle batching
   * the transaction in the importUserData() method.</b>
   */
  public void executeImport() throws VersionException, WorkflowException, CreateException, ActionException,
            TransactionDemarcationException, Exception
  {
    TransactionDemarcation td = new TransactionDemarcation();
    boolean rollback = true;
    Process process = null;
    try {
      td.begin(getTransactionManager());

      assumeUserIdentity();

      String projectName = getProjectName();

      ProcessHome processHome = ProjectConstants.getPersistentHomes().getProcessHome();
      if (getActivityId() != null) {
        process = processHome.createProcessForImport(projectName, getWorkflowName(), getActivityId() );
      }
      else {
       process = processHome.createProcessForImport(projectName, getWorkflowName() );
      }
      
      String wkspName = process.getProject().getWorkspace();
      Workspace wksp = getVersionManager().getWorkspaceByName(wkspName);
      WorkingContext.pushDevelopmentLine(wksp);

      importUserData(process, td);

      rollback = false;
    } catch (VersionException e) {
      throw e;
    } catch (TransactionDemarcationException e) {
      throw e;
    } catch (CreateException e) {
      throw e;
    } catch (WorkflowException e) {
      throw e;
    } catch (ActionException e) {
      throw e;
    } catch (Exception e) {
      throw e;
    } finally {
      try {
        td.end(rollback);
      } catch (TransactionDemarcationException tde) {
        throw tde;
      }
      WorkingContext.popDevelopmentLine();
    }

    td = new TransactionDemarcation();
    rollback = true;

    try {
      td.begin(getTransactionManager());

      advanceWorkflow(process);

      rollback = false;
    } catch (TransactionDemarcationException e) {
      throw e;
    } catch (WorkflowException e) {
      throw e;
    } catch (ActionException e) {
      throw e;
    } catch (Exception e) {
      throw e;
    } finally {
      releaseUserIdentity();
      try {
        td.end(rollback);
      } catch (TransactionDemarcationException tde) {
        throw tde;
      }
    }

  }

  //-------------------------------------
  /**
   * This method advances the workflow to the next state. If using an unaltered copy of the import-late
   * or import-early workflows, then the taskOutcomeId property should not need to be changed
   * (default is '4.1.1'). If you are using a different workflow or an altered version of the import-xxxx
   * workflows, then the taskOutcomeId can be found in the wdl file for the respective workflow.
   *
   * @param pProcess the atg.epub.project.Process object (the project)
   */
  protected void advanceWorkflow(Process pProcess) throws WorkflowException, ActionException
  {
    RepositoryItem processWorkflow = pProcess.getProject().getWorkflow();
    String workflowProcessName = processWorkflow.getPropertyValue("processName").toString();
    String subjectId = pProcess.getId();

    try {
      // an alternative would be to use the global workflow view at
      WorkflowView wv = getWorkflowManager().getWorkflowView(ThreadSecurityManager.currentUser());

      wv.fireTaskOutcome(workflowProcessName, WorkflowConstants.DEFAULT_WORKFLOW_SEGMENT,
                          subjectId,
                          getTaskOutcomeId(),
                          ActionConstants.ERROR_RESPONSE_DEFAULT);

    } catch (MissingWorkflowDescriptionException e) {
      throw e;
    } catch (ActorAccessException e) {
      throw e;
    } catch (ActionException e) {
      throw e;
    } catch (UnsupportedOperationException e) {
      throw e;
    }
  }

  //-------------------------------------
  /**
   * This method sets the security context for the current thread so that the code executes correctly
   * against secure resources.
   *
   * @return true if the identity was assumed, false otherwise
   */
  protected boolean assumeUserIdentity() {
    if (getUserAuthority() == null)
        return false;

    User newUser = new User();
    Persona persona = (Persona) getUserAuthority().getPersona(getPersonaPrefix() + getUserName());
    if (persona == null)
        return false;

    // create a temporary User object for the identity
    newUser.addPersona(persona);

    // replace the current User object
    ThreadSecurityManager.setThreadUser(newUser);

    return true;
  }

  //-------------------------------------
  /**
   * This method unsets the security context on the current thread.
   */
  protected void releaseUserIdentity()
  {
    ThreadSecurityManager.setThreadUser(null);
  }
}