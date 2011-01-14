/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.tigris.subversion.svnant;

import org.apache.tools.ant.BuildException;

import java.util.Collection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * DBC variety used for Ant. The difference is the fact that a BuildException is produced instead of an 
 * IllegalArgumentException. Another difference is the fact that there will be Ant related messages.
 * This function also provides a set of useful helper functions.
 */
public class SvnAntUtilities {

  /**
   * Prevent instantiation.
   */
  private SvnAntUtilities() {
  }
  
  public static final void close( InputStream stream ) {
      if( stream != null ) {
          try {
              stream.close();
          } catch( IOException ex ) {
              
          }
      }
  }

  public static final void close( OutputStream stream ) {
      if( stream != null ) {
          try {
              stream.close();
          } catch( IOException ex ) {
              
          }
      }
  }

  /**
   * Causes a BuildException if all supplied objects are <code>null</code> or all collections
   * are empty.
   * 
   * @param params      A list of parameters that do represent the supplied objects. 
   *                    Neither <code>null</code> nor empty.
   * @param objects     The objects that needs to be tested.
   */
  public static final void attrsNotSet( String params, Object ... objects ) {
      attrsNotSet( params, false, objects );
  }
  
  /**
   * Causes a BuildException if all supplied objects are <code>null</code> or all collections
   * are empty.
   * 
   * @param params      A list of parameters that do represent the supplied objects. 
   *                    Neither <code>null</code> nor empty.
   * @param exclusive   <code>true</code> <=> Only one value is allowed to be set.
   * @param objects     The objects that needs to be tested.
   */
  public static final void attrsNotSet( String params, boolean exclusive, Object ... objects ) {
      int valid = 0;
      if( objects != null ) {
          for( Object obj : objects ) {
              if( obj instanceof Collection ) {
                  if( ((Collection<?>) obj).size() > 0 ) {
                      // the collection is not empty, so we've got a value
                      valid++;
                  }
              } else if( obj != null ) {
                  // the object is not null, so we've got a value
                  valid++;
              }
          }
      }
      if( valid == 0 ) {
          throw create( "At least one value of {%s} has to be set.", params );
      }
      if( exclusive ) {
          if( valid > 1 ) {
              throw create( "Only one value of {%s} is allowed to be set.", params );
          }
      }
  }
  
  /**
   * Causes a BuildException if an object is null.
   * 
   * @param param   The name for the parameter. Neither <code>null</code> nor empty.
   * @param obj     The object which has to be tested.
   */
  public static final void attrNotNull( String param, Object obj ) {
    if( obj == null ) {
      throw create( "The attribute '%s' has not been set.", param );
    }
  }

  /**
   * Causes a BuildException if an object is not null.
   * 
   * @param param   The name for the parameter. Neither <code>null</code> nor empty.
   * @param obj     The object which has to be tested.
   */
  public static final void attrNull( String param, Object obj ) {
    if( obj != null ) {
      throw create( "The attribute '%s' is not allowed to be set.", param );
    }
  }

  /**
   * Causes a BuildException if a File is not a File.
   * 
   * @param param   The name for the parameter. Neither <code>null</code> nor empty.
   * @param file    The File which has to be tested.
   */
  public static final void attrIsFile( String param, File file ) {
    attrNotNull( param, file );
    if( ! file.isFile() ) {
      throw create( "The attribute '%s' with the value '%s' is not a file.", param, file );
    }
  }

  /**
   * Causes a BuildException if a resource exists.
   * 
   * @param param   The name for the parameter. Neither <code>null</code> nor empty.
   * @param file    The File which has to be tested.
   */
  public static final void attrExists( String param, File file ) {
    attrNotNull( param, file );
    if( ! file.exists() ) {
      throw create( "The attribute '%s' with the value '%s' doesn't denote an existing resource.", param, file );
    }
  }

  /**
   * Causes a BuildException if a File is not a directory.
   * 
   * @param param       The name for the parameter. Neither <code>null</code> nor empty.
   * @param directory   The File which has to be tested.
   */
  public static final void attrIsDirectory( String param, File directory ) {
    attrNotNull( param, directory );
    if( ! directory.isDirectory() ) {
      throw create( "The attribute '%s' with the value '%s' is not a directory.", param, directory );
    }
  }
  
  /**
   * Causes a BuildException if a File cannot be written to.
   * 
   * @param param   The name for the parameter. Neither <code>null</code> nor empty.
   * @param file    The path which has to be tested.
   */
  public static final void attrCanWrite( String param, File file ) {
    attrNotNull( param, file );
    if( file.exists() ) {
      if( file.isDirectory() || (! file.canWrite()) ) {
        throw create( "The attribute '%s' which denotes the file '%s' doesn't specify a writable file.", param, file );
      }
    }
  }
  
  /**
   * Causes a BuildException if the supplied String is <code>null</code> or empty.
   * 
   * @param param   The name for the parameter. Neither <code>null</code> nor empty.
   * @param value   The value which has to be tested.
   */
  public static final void attrNotEmpty( String param, String value ) {
    attrNotNull( param, value );
    if( value.length() == 0 ) {
      throw create( "The attribute '%s' is neither allowed to be null nor empty.", param );
    }
  }

  /**
   * Causes a BuildException if the supplied object is not one of a list of possible candidates.
   * 
   * @param param     The name of the parameter. Neither <code>null</code> nor empty.
   * @param allowed   The list of allowed values. Not <code>null</code>.
   * @param value     The value which has to be tested.
   */
  public static final void attrInvalidValue( String param, Object[] allowed, String value ) {
    attrNotNull( param, value );
    StringBuffer text = new StringBuffer();
    for( Object valid : allowed ) {
      if( value.equals( String.valueOf( valid ) ) ) {
        return;
      }
      if( text.length() > 0 ) {
          text.append( ", " );
      }
      text.append( String.valueOf( valid ) );
    }
    throw create( "The attribute '%s' has the value '%s' but only the following values are allowed: {%s}.", param, value, text );
  }
  
  /**
   * Creates a BuildException using the supplied arguments. If the last argument is an instance of
   * Exception it will be used as the causing exception rather than an argument.
   * 
   * @param fmt    Formatting String for the error message. Neither <code>null</code> nor empty.
   * @param args   The arguments to be passed to the formatting message. Maybe <code>null</code>.
   * 
   * @return   A BuildException indicating the error cause. Not <code>null</code>.
   */
  private static final BuildException create( String fmt, Object ... args ) {
    if( (args == null) || (args.length == 0) ) {
      return new BuildException( fmt );
    } else {
      Object last = args[ args.length - 1 ];
      if( last instanceof Exception ) {
        // we don't need to reduce the argument array as the last argument is just not being
        // used (even if it would be used it just would be redundant).
        return new BuildException( String.format( fmt, args ), (Exception) last );
      } else {
        return new BuildException( String.format( fmt, args ) );
      }
    }
  }

} /* ENDCLASS */
