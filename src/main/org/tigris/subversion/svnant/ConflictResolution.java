/**
 * A simple mapping for resolutions that can be specified using ant.
 * 
 * @author Daniel Kasmeroglu
 *         <a href="mailto:daniel.kasmeroglu@kasisoft.net">daniel.kasmeroglu@kasisoft.net</a>
 *
 */
package org.tigris.subversion.svnant;

import org.tigris.subversion.svnclientadapter.SVNConflictDescriptor;
import org.tigris.subversion.svnclientadapter.SVNConflictResult;

/**
 * A simple mapping for resolutions that can be specified using ant.
 * 
 * @author Daniel Kasmeroglu
 *         <a href="mailto:daniel.kasmeroglu@kasisoft.net">daniel.kasmeroglu@kasisoft.net</a>
 */
public enum ConflictResolution {

    Postpone        ( SVNConflictResult . postpone          ),
    Base            ( SVNConflictResult . chooseBase        ),
    TheirsFull      ( SVNConflictResult . chooseTheirsFull  ),
    MineFull        ( SVNConflictResult . chooseMineFull    ),
    TheirsConflict  ( SVNConflictResult . chooseTheirs      ),
    MineConflict    ( SVNConflictResult . chooseMine        ),
    Merged          ( SVNConflictResult . chooseMerged      );

    private int   resolution;
    
    ConflictResolution( int svnresolution ) {
        resolution = svnresolution;
    }
    
    /**
     * Generates a resolution response for the supplied descriptor.
     * 
     * @param descriptor   The descriptor instance provided by the client. Not <code>null</code>.
     * 
     * @return   The resolution used by the client as a hint to handle the conflict. Not <code>null</code>.
     */
    public SVNConflictResult resolve( SVNConflictDescriptor descriptor ) {
        switch( this ) {
        case Postpone       : return new SVNConflictResult( resolution, descriptor.getPath() );
        case Base           : return new SVNConflictResult( resolution, descriptor.getBasePath() );
        case TheirsFull     : return new SVNConflictResult( resolution, descriptor.getTheirPath() );
        case MineFull       : return new SVNConflictResult( resolution, descriptor.getMyPath() );
        case TheirsConflict : return new SVNConflictResult( resolution, descriptor.getTheirPath() );
        case MineConflict   : return new SVNConflictResult( resolution, descriptor.getMyPath() );
        case Merged         : return new SVNConflictResult( resolution, descriptor.getMergedPath() );
        }
        // won't happen
        return null;
    }
    
} /* ENDENUM */
