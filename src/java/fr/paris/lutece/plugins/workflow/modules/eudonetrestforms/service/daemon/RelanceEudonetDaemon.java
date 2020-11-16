/*
 * Copyright (c) 2002-2014, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */

package fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.service.daemon;

import java.util.List;

import fr.paris.lutece.portal.service.daemon.Daemon;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.workflow.WorkflowService;

public class RelanceEudonetDaemon extends Daemon
{
    public static final String PROPERTY_RESOURCE_TYPE        = AppPropertiesService.getProperty( "workflow.eudonetrelance.resource_type" );
    public static final int    PROPERTY_STATE_BEFORE_EUDONET = AppPropertiesService.getPropertyInt( "workflow.eudonetrelance.state_eudonet", 8 );
    public static final int    PROPERTY_ACTION_EUDONET       = AppPropertiesService.getPropertyInt( "workflow.eudonetrelance.action_eudonet", 9 );

    @Override
    public void run( )
    {
        List<Integer> listResourceBeforeEudonet = WorkflowService.getInstance( ).getResourceIdListByIdState( PROPERTY_STATE_BEFORE_EUDONET, PROPERTY_RESOURCE_TYPE );
        int ctr = 0;
        if ( ( listResourceBeforeEudonet != null ) && !listResourceBeforeEudonet.isEmpty( ) )
        {
            for ( Integer resourceBeforeEudonet : listResourceBeforeEudonet )
            {
                try
                {

                    WorkflowService.getInstance( ).doProcessAction( resourceBeforeEudonet, PROPERTY_RESOURCE_TYPE, PROPERTY_ACTION_EUDONET, -1, null, null, true );
                    AppLogService.info( "Demand " + resourceBeforeEudonet + " successfully exported" );
                    ctr++;
                } catch ( Exception e )
                {
                    AppLogService.error( "Error on daemon export demand to eudonet", e );
                }
            }
        }
        if ( ctr > 0 )
        {
            AppLogService.info( ctr + " demand exported" );
        } else
        {
            AppLogService.info( "No demand exported" );
        }
    }
}
