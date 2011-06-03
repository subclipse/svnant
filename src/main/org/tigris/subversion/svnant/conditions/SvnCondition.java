package org.tigris.subversion.svnant.conditions;


import org.tigris.subversion.svnant.SvnFacade;

import org.apache.tools.ant.types.Reference;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;

/**
 * This is an abstract class that implements functions common to all subclasses.
 * It handles the generic 'javahl' and 'svnkit' properties.
 * 
 * Conditions are from a special type in ANT where they can be used to set values
 * of properties. See taks Condition in ANT documentation.
 * 
 * @author Jean-Pierre Fiset <a href="mailto:jp@fiset.ca">jp@fiset.ca</a>
 * @author Daniel Kasmeroglu <a href="mailto:daniel.kasmeroglu@kasisoft.net">Daniel.Kasmeroglu@kasisoft.net</a>
 */
public abstract class SvnCondition extends ConditionBase implements Condition {

    private static final String MSG_DEPRECATION = 
        "Deprecated attribute '%s'. This attribute will disappear with SVNANT 1.3.2. Use svnSetting instead.";

    /**
     * @see SvnFacade#setRefid(ProjectComponent, org.apache.tools.ant.types.Reference)
     */
    public void setRefid( Reference refid ) {
        SvnFacade.setRefid( this, refid );
    }

    /**
     * @see SvnFacade#setUsername(ProjectComponent, String)
     */
    public void setUsername( String username ) {
        warning( MSG_DEPRECATION, "username" );
        SvnFacade.setUsername( this, username );
    }

    /**
     * @see SvnFacade#setPassword(ProjectComponent, String)
     */
    public void setPassword( String password ) {
        warning( MSG_DEPRECATION, "password" );
        SvnFacade.setPassword( this, password );
    }

    /**
     * @see SvnFacade#setJavahl(ProjectComponent, boolean)
     */
    public void setJavahl( boolean javahl ) {
        warning( MSG_DEPRECATION, "javahl" );
        SvnFacade.setJavahl( this, javahl );
    }

    /**
     * @see SvnFacade#setSvnKit(ProjectComponent, boolean)
     */
    public void setSvnkit( boolean svnkit ) {
        warning( MSG_DEPRECATION, "svnkit" );
        SvnFacade.setSvnKit( this, svnkit );
    }

    /**
     * {@inheritDoc}
     */
    public boolean eval() {
        preconditions();
        return internalEval();
    }
    
    /**
     * Causes a BuildException to be thrown if a precondition isn't met.
     * 
     * @throws BuildException   A precondition isn't fulfilled.
     */
    protected void preconditions() throws BuildException {
    }
    
    /**
     * Method called internally by eval(). Must be implemented by all subclasses.
     * @return True if the condition is met. False, otherwsie.
     */
    protected abstract boolean internalEval();
    
    /**
     * Dumps some warning messages.
     * 
     * @param fmt    A formatting String. Not <code>null</code>.
     * @param args   The arguments for the formatting String. Maybe <code>null</code>.
     */
    private void warning( String fmt, Object ... args ) {
        getProject().log( String.format( fmt, args ), Project.MSG_WARN );
    }
    
}
