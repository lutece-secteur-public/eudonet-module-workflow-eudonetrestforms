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
package fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.sun.jersey.api.client.ClientResponse;

import fr.paris.lutece.plugins.forms.business.Form;
import fr.paris.lutece.plugins.forms.business.FormHome;
import fr.paris.lutece.plugins.forms.business.Question;
import fr.paris.lutece.plugins.forms.business.QuestionHome;
import fr.paris.lutece.plugins.forms.business.Step;
import fr.paris.lutece.plugins.forms.business.StepHome;
import fr.paris.lutece.plugins.forms.service.FormsPlugin;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.business.EudonetRestData;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.business.ReferenceItemSorted;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.business.TaskEudonetRestConfig;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.business.TaskEudonetRestConfigHome;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.service.EudonetClient;
import fr.paris.lutece.plugins.workflow.modules.eudonetrestforms.utils.EudonetRestFormsConstants;
import fr.paris.lutece.plugins.workflow.utils.WorkflowUtils;
import fr.paris.lutece.plugins.workflow.web.task.NoFormTaskComponent;
import fr.paris.lutece.plugins.workflowcore.service.config.ITaskConfigService;
import fr.paris.lutece.plugins.workflowcore.service.task.ITask;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.ReferenceItem;
import fr.paris.lutece.util.ReferenceList;
import fr.paris.lutece.util.html.HtmlTemplate;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * EudonetTaskComponent
 *
 */
public class EudonetRestTaskComponent extends NoFormTaskComponent
{
    // MARKS
    private static final String MARKER_TASK_EUDONET_CONFIG = "taskConfig";
    private static final String MARKER_LIST_ATTRIBUT_EUDONET = "list_attribut";
    private static final String MARKER_LIST_TABLE_EUDONET = "list_tableEudonet";
    private static final String MARKER_LIST_TABLE_EUDONET_LINK = "list_tableEudonet_link";
    private static final String MARKER_ENTRY = "entries";

    // TEMPLATES
    private static final String TEMPLATE_TASK_EUDONET = "admin/plugins/workflow/modules/eudonetrestforms/task_export_eudonet_config.html";

    // PARAMETERS
    public static final String PARAMETER_ID_FORMS = "id_forms";
    public static final String PARAMETER_ID_TELESERVICE = "id_teleservice";
    public static final String PARAMETER_ID_TABLE_EUDONET = "id_tableEudonet";
    public static final String PARAMETER_ID_TABLE_EUDONET_LINK = "id_tableEudonet_link";
    public static final String PARAMETER_ENTRY_DEFAULT_VALUE = "entry_default_value";
    public static final String PARAMETER_BASE_URL = "base_url";
    public static final String PARAMETER_SUBSCRIBER_LOGIN = "subscriber_login";
    public static final String PARAMETER_SUBSCRIBER_PASSWORD = "subscriber_password";
    public static final String PARAMETER_BASE_NAME = "base_name";
    public static final String PARAMETER_USER_LOGIN = "user_login";
    public static final String PARAMETER_USER_PASSWORD = "user_password";
    public static final String PARAMETER_USER_LANG = "user_lang";
    public static final String PARAMETER_PRODUCT_NAME = "product_name";
    public static final String PARAMETER_CREAT_ENTRY = "apply";
    public static final String PARAMETER_DELETE_ENTRY = "deleteEntry";
    public static final String PARAMETER_CREATE_ENTRY = "createEntry";
    public static final String PARAMETER_ORDER_ENTRY = "order_entry";
    public static final String PARAMETER_EUDONET_ATTRIBUT = "eudonet_attribut";
    public static final String PARAMETER_EUDONET_TABLE = "eudonet_table";
    public static final String PARAMETER_ID_ENTRY = "id_entry";

    // Constants
    private static final String CONSTANT_ENTRY_DATE_CREATION = "-2";
    private static final String CONSTANT_ENTRY_DATE_MODIFICATION = "-3";

    public static final String CONSTANT_I18_LABEL_ENTRY_DATE_CREATION = "module.workflow.eudonetrestforms.task_eudonet_config.label.creation_date";
    public static final String CONSTANT_I18_LABEL_ENTRY_DATE_MODIFICATION = "module.workflow.eudonetrestforms.task_eudonet_config.label.modification_date";

    // SERVICES
    @Inject
    @Named( EudonetRestFormsConstants.BEAN_EUDONET_FORMS_CONFIG_SERVICE )
    private ITaskConfigService _taskEudonetConfigService;

    private EudonetClient _client;
    private TaskEudonetRestConfig _config;

    /**
     * {@inheritDoc}
     */
    @Override
    public String doSaveConfig( HttpServletRequest request, Locale locale, ITask task )
    {
        String idForms = request.getParameter( PARAMETER_ID_FORMS );
        String idTableEudonet = StringUtils.isEmpty( request.getParameter( PARAMETER_ID_TABLE_EUDONET ) ) ? "-1" : request.getParameter( PARAMETER_ID_TABLE_EUDONET );
        String baseUrl = request.getParameter( PARAMETER_BASE_URL );
        String subscriberLogin = request.getParameter( PARAMETER_SUBSCRIBER_LOGIN );
        String subscriberPassword = request.getParameter( PARAMETER_SUBSCRIBER_PASSWORD );
        String baseName = request.getParameter( PARAMETER_BASE_NAME );
        String userLogin = request.getParameter( PARAMETER_USER_LOGIN );
        String userPassword = request.getParameter( PARAMETER_USER_PASSWORD );
        String userLang = request.getParameter( PARAMETER_USER_LANG );
        String productName = request.getParameter( PARAMETER_PRODUCT_NAME );

        _config = _taskEudonetConfigService.findByPrimaryKey( task.getId( ) );
        Boolean bCreate = false;

        if ( _config == null )
        {
            _config = new TaskEudonetRestConfig( );
            _config.setIdTask( task.getId( ) );
            bCreate = true;
        }

        if ( ( ( request.getParameter( PARAMETER_CREAT_ENTRY ) != null ) && PARAMETER_CREATE_ENTRY.equals( request.getParameter( PARAMETER_CREAT_ENTRY ) ) )
                && ( Integer.parseInt( idForms ) != WorkflowUtils.CONSTANT_ID_NULL ) )
        {
            String ordreEntry = request.getParameter( PARAMETER_ORDER_ENTRY ).equals( "" ) ? "-1" : request.getParameter( PARAMETER_ORDER_ENTRY );
            String eudonetAttribut = request.getParameter( PARAMETER_EUDONET_ATTRIBUT );
            String eudonetTable = request.getParameter( PARAMETER_ID_TABLE_EUDONET );
            String eudonetTableLink = request.getParameter( PARAMETER_ID_TABLE_EUDONET_LINK );
            String eudonetEntryDefaulValue = request.getParameter( PARAMETER_ENTRY_DEFAULT_VALUE );

            EudonetRestData data = new EudonetRestData( );
            data.setIdConfig( task.getId( ) );

            if ( eudonetEntryDefaulValue != null && !eudonetEntryDefaulValue.isEmpty( ) )
            {
                data.setDefaultValue( eudonetEntryDefaulValue );
            }
            else
            {
                data.setOrderQuestion( ordreEntry );
            }

            data.setIdTable( eudonetTable );
            data.setIdAttribut( eudonetAttribut );
            data.setIdTableLink( eudonetTableLink );

            TaskEudonetRestConfigHome.creatEntry( data );

            return null;
        }
        else
            if ( ( request.getParameter( PARAMETER_ID_ENTRY ) != null ) && ( request.getParameter( PARAMETER_CREAT_ENTRY ) != null )
                    && PARAMETER_DELETE_ENTRY.equals( request.getParameter( PARAMETER_CREAT_ENTRY ) )
                    && ( Integer.parseInt( idForms ) != WorkflowUtils.CONSTANT_ID_NULL ) )
            {
                String idEntry = request.getParameter( PARAMETER_ID_ENTRY );
                TaskEudonetRestConfigHome.deleteEntry( Integer.parseInt( idEntry ) );

                return null;
            }

        _config.setIdForms( Integer.parseInt( idForms ) );
        _config.setIdTableEudonet( idTableEudonet );
        _config.setBaseUrl( baseUrl );
        _config.setSubscriberLogin( subscriberLogin );
        _config.setSubscriberPassword( subscriberPassword );
        _config.setBaseName( baseName );
        _config.setUserLogin( userLogin );
        _config.setUserPassword( userPassword );
        _config.setUserLang( userLang );
        _config.setProductName( productName );

        if ( _client == null )
        {
            _client = new EudonetClient( _config.getBaseUrl( ) );
        }

        if ( bCreate )
        {
            _taskEudonetConfigService.create( _config );
        }
        else
        {
            _taskEudonetConfigService.update( _config );
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayConfigForm( HttpServletRequest request, Locale locale, ITask task )
    {
        Map<String, Object> model = new HashMap<String, Object>( );
        String strIdTask = request.getParameter( EudonetRestFormsConstants.PARAMETER_ID_TASK );

        int nIdForms;

        if ( StringUtils.isNotBlank( request.getParameter( EudonetRestFormsConstants.PARAMETER_ID_FORMS ) ) )
        {
            nIdForms = Integer.valueOf( request.getParameter( EudonetRestFormsConstants.PARAMETER_ID_FORMS ) );
        }
        else
        {
            nIdForms = -1;
        }

        if ( StringUtils.isNotBlank( strIdTask ) )
        {
            _config = _taskEudonetConfigService.findByPrimaryKey( Integer.valueOf( strIdTask ) );

            if ( _config != null )
            {
                model.put( MARKER_TASK_EUDONET_CONFIG, _config );

                List<EudonetRestData> entries = (List<EudonetRestData>) _config.getEntry( );

                model.put( MARKER_ENTRY, entries );
                nIdForms = _config.getIdForms( );
            }
            else
            {
                model.put( MARKER_TASK_EUDONET_CONFIG, new TaskEudonetRestConfig( ) );
            }
        }

        model.put( EudonetRestFormsConstants.MARK_FORMS_LIST, getListForms( ) );
        model.put( EudonetRestFormsConstants.MARK_LIST_ENTRIES, getListQuestions( nIdForms, request ) );
        model.put( MARKER_LIST_TABLE_EUDONET, getEudonetTables( ) );
        model.put( MARKER_LIST_TABLE_EUDONET_LINK, getEudonetTables( ) );
        model.put( MARKER_LIST_ATTRIBUT_EUDONET, getEudonetAttribut( ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_TASK_EUDONET, locale, model );

        return template.getHtml( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDisplayTaskInformation( int nIdHistory, HttpServletRequest request, Locale locale, ITask task )
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTaskInformationXml( int nIdHistory, HttpServletRequest request, Locale locale, ITask task )
    {
        return null;
    }

    /**
     * Get the list of forms
     * 
     * @return a ReferenceList
     */
    private static ReferenceList getListForms( )
    {
        List<Form> listForms = FormHome.getFormList( );
        ReferenceList referenceList = new  ReferenceList();
        if ( CollectionUtils.isNotEmpty(listForms) )
        {
        	List<ReferenceItem> list = listForms.stream().map(form -> {
        		ReferenceItem item = new ReferenceItem();
        		item.setCode(String.valueOf(form.getId()));
        		item.setName(form.getTitle());
        		return item;
        	}).collect(Collectors.toList());
        	referenceList.addAll( list );
        }

        return referenceList;
    }
    
    /**
     * Method to get eudonet attributs list
     * 
     * @return tables list
     */
    private List<ReferenceItemSorted> getEudonetTables( )
    {
        ReferenceList tableList = new ReferenceList( );

        List<ReferenceItemSorted> referenceList = new ArrayList<ReferenceItemSorted>( );

        ReferenceItemSorted referenceItemSorted = new ReferenceItemSorted( );
        referenceItemSorted.setCode( "" );
        referenceItemSorted.setName( "" );
        referenceList.add( referenceItemSorted );

        if ( _config != null )
        {
            if ( _client == null )
            {
                _client = new EudonetClient( _config.getBaseUrl( ) );
            }

            String strToken = token( );
            JSONArray tableListJson = getTableListJson( strToken );
            tableList = getTableList( tableListJson );

            for ( ReferenceItem item : tableList )
            {
                ReferenceItemSorted referenceItem = new ReferenceItemSorted( );
                referenceItem.setCode( item.getCode( ) + "-" + item.getName( ) );
                referenceItem.setName( item.getName( ) );
                referenceList.add( referenceItem );
            }
        }

        Collections.sort( referenceList );

        return referenceList;
    }

    /**
     * Method to get eudonet tables list
     * 
     * @return attributs list
     */
    private List<ReferenceItemSorted> getEudonetAttribut( )
    {
        List<ReferenceItemSorted> referenceList = new ArrayList<ReferenceItemSorted>( );

        ReferenceItemSorted referenceItemSorted = new ReferenceItemSorted( );
        referenceItemSorted.setCode( "" );
        referenceItemSorted.setName( "" );
        referenceList.add( referenceItemSorted );

        ReferenceList attributList = new ReferenceList( );

        if ( _config != null )
        {
            if ( _client == null )
            {
                _client = new EudonetClient( _config.getBaseUrl( ) );
            }

            String strToken = token( );

            String strIdTableEudonet = _config.getIdTableEudonet( ).split( "-" ) [0];
            
            if ( StringUtils.isNotEmpty(strIdTableEudonet) ) {
                JSONArray attributListJson = getAttributListJson( strToken, strIdTableEudonet );

                attributList = getAttributList( attributListJson );

                for ( ReferenceItem item : attributList )
                {
                    boolean isContain = false;

                    for ( EudonetRestData ent : _config.getEntry( ) )
                    {
                        String strIdattEudonet = item.getCode( ) + "-" + item.getName( );
                        if ( strIdattEudonet.equals( ent.getIdAttribut( ) ) )
                        {
                            isContain = true;

                            break;
                        }
                    }

                    if ( !isContain )
                    {
                        ReferenceItemSorted referenceItem = new ReferenceItemSorted( );
                        referenceItem.setCode( item.getCode( ) + "-" + item.getName( ) );
                        referenceItem.setName( item.getName( ) );
                        referenceList.add( referenceItem );
                    }
                }
            }
        }

        Collections.sort( referenceList );

        return referenceList;
    }

    /**
     * Method to get directory entries list
     * 
     * @param nIdDirectory
     *            id directory
     * @param request
     *            request
     * @return ReferenceList entries list
     */
    private static List<ReferenceItemSorted> getListQuestions( int nIdForm, HttpServletRequest request )
    {
        if ( nIdForm != -1 )
        {
            Plugin pluginDirectory = PluginService.getPlugin( FormsPlugin.PLUGIN_NAME );
            
            Form form = FormHome.findByPrimaryKey( nIdForm );
            
//            List<Step> listStepsOfForm = StepHome.getStepsListByForm( form.getId( ) );
            
            List<ReferenceItemSorted> referenceList = new ArrayList<ReferenceItemSorted>( );
			List<Question> listQuestionByIdForm = QuestionHome.getListQuestionByIdForm( form.getId( ) );
			
			listQuestionByIdForm.stream().forEach(question -> {
				ReferenceItemSorted referenceItem = new ReferenceItemSorted( );
				referenceItem.setCode( question.getCode() );
				referenceItem.setName( question.getTitle() );
	            referenceList.add( referenceItem );
				
			});
			
//            List<IEntry> listEntries = DirectoryUtils.getFormEntries( nIdDirectory, pluginDirectory, AdminUserService.getAdminUser( request ) );
//            List<ReferenceItemSorted> referenceList = new ArrayList<ReferenceItemSorted>( );
//
//            ReferenceItemSorted referenceItemSorted = new ReferenceItemSorted( );
//            referenceItemSorted.setCode( "" );
//            referenceItemSorted.setName( "" );
//            referenceList.add( referenceItemSorted );
//
//            ReferenceItemSorted referenceItemSortedCD = new ReferenceItemSorted( );
//            referenceItemSortedCD.setCode( CONSTANT_ENTRY_DATE_CREATION );
//            referenceItemSortedCD.setName( I18nService.getLocalizedString( CONSTANT_I18_LABEL_ENTRY_DATE_CREATION, request.getLocale( ) ) );
//            referenceList.add( referenceItemSortedCD );
//
//            ReferenceItemSorted referenceItemSortedMD = new ReferenceItemSorted( );
//            referenceItemSortedMD.setCode( CONSTANT_ENTRY_DATE_MODIFICATION );
//            referenceItemSortedMD.setName( I18nService.getLocalizedString( CONSTANT_I18_LABEL_ENTRY_DATE_MODIFICATION, request.getLocale( ) ) );
//            referenceList.add( referenceItemSortedMD );
//
//            for ( IEntry entry : listEntries )
//            {
//                if ( entry.getEntryType( ).getComment( ) )
//                {
//                    continue;
//                }
//
//                if ( entry.getEntryType( ).getGroup( ) )
//                {
//                    if ( entry.getChildren( ) != null )
//                    {
//                        for ( IEntry child : entry.getChildren( ) )
//                        {
//                            if ( child.getEntryType( ).getComment( ) )
//                            {
//                                continue;
//                            }
//
//                            ReferenceItemSorted referenceItem = new ReferenceItemSorted( );
//                            referenceItem.setCode( String.valueOf( child.getIdEntry( ) ) );
//                            referenceItem.setName( child.getTitle( ) );
//                            referenceList.add( referenceItem );
//                        }
//                    }
//                }
//                else
//                {
//                    ReferenceItemSorted referenceItem = new ReferenceItemSorted( );
//                    referenceItem.setCode( String.valueOf( entry.getIdEntry( ) ) );
//                    referenceItem.setName( entry.getTitle( ) );
//                    referenceList.add( referenceItem );
//                }
//            }

            Collections.sort( referenceList );

            return referenceList;
        }
        else
        {
            return new ArrayList<ReferenceItemSorted>( );
        }
    }

    public String getAuthenticateJsonBody( )
    {
        JSONObject jsonObject = new JSONObject( );
        jsonObject.accumulate( "SubscriberLogin", _config.getSubscriberLogin( ) );
        jsonObject.accumulate( "SubscriberPassword", _config.getSubscriberPassword( ) );
        jsonObject.accumulate( "BaseName", _config.getBaseName( ) );
        jsonObject.accumulate( "UserLogin", _config.getUserLogin( ) );
        jsonObject.accumulate( "UserPassword", _config.getUserPassword( ) );
        jsonObject.accumulate( "UserLang", _config.getUserLang( ) );
        jsonObject.accumulate( "ProductName", _config.getProductName( ) );

        return jsonObject.toString( );
    }

    public String getAttributListJsonBody( String strIdTable )
    {
        if ( strIdTable != null )
        {
            // int nIdTable = Integer.parseInt(strIdTable);
            JSONObject jsonObject = new JSONObject( );
            JSONArray jsonArray = new JSONArray( );
            JSONObject jsonElement = new JSONObject( );
            JSONArray jsonElementArray = new JSONArray( );

            jsonElementArray.add( Integer.parseInt( strIdTable ) );

            jsonElement.accumulate( "DescId", Integer.parseInt( strIdTable ) );
            jsonElement.accumulate( "AllFields", true );
            jsonElement.accumulate( "Fields", jsonElementArray );

            jsonArray.add( jsonElement );

            jsonObject.accumulate( "Tables", jsonArray );

            return jsonObject.toString( );
        }

        return null;
    }

    public String token( )
    {
        String strAuthenticateJson = getAuthenticateJsonBody( );
        try
        {
            ClientResponse response = _client.getTokenAuthenticate( strAuthenticateJson );

            if ( response.getStatus( ) == 200 )
            {
                String strResponse = response.getEntity( String.class );
                JSONObject jsonObject = new JSONObject( );
                jsonObject.accumulate( "object", strResponse );
                String strStatus = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "Success" );
                if ( strStatus.equals( "true" ) )
                {
                    return jsonObject.getJSONObject( "object" ).getJSONObject( "ResultData" ).getString( "Token" );
                }
                else
                {
                    String strErrorMessage = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "ErrorMessage" );
                    AppLogService.error( "Error Eudonet : " + strErrorMessage );
                }
            }
        }
        catch( Exception ex )
        {
            AppLogService.error( "Erreur to get a token", ex );
        }

        return null;

    }

    public JSONArray getTableListJson( String strToken )
    {
        if ( strToken != null )
        {
            try
            {
                ClientResponse response = _client.getTableListMetaInfos( strToken );
                if ( response.getStatus( ) == 200 )
                {
                    String strResponse = response.getEntity( String.class );
                    JSONObject jsonObject = new JSONObject( );
                    jsonObject.accumulate( "object", strResponse );
                    String strStatus = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "Success" );
                    if ( strStatus.equals( "true" ) )
                    {
                        return jsonObject.getJSONObject( "object" ).getJSONObject( "ResultMetaData" ).getJSONArray( "Tables" );
                    }
                    else
                    {
                        String strErrorMessage = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "ErrorMessage" );
                        AppLogService.error( "Error Eudonet : " + strErrorMessage );
                    }
                }

            }
            catch( Exception ex )
            {
                AppLogService.error( "Erreur to generate the table list of eudonet", ex );
            }
        }

        return null;
    }

    public JSONArray getAttributListJson( String strToken, String strIdTable )
    {
        if ( strToken != null )
        {
            try
            {
                String strBody = getAttributListJsonBody( strIdTable );
                if ( strBody != null )
                {
                    ClientResponse response = _client.getAttributListMetaInfos( strToken, strBody );
                    if ( response.getStatus( ) == 200 )
                    {
                        String strResponse = response.getEntity( String.class );
                        JSONObject jsonObject = new JSONObject( );
                        jsonObject.accumulate( "object", strResponse );
                        String strStatus = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "Success" );
                        if ( strStatus.equals( "true" ) )
                        {
                            return jsonObject.getJSONObject( "object" ).getJSONObject( "ResultMetaData" ).getJSONArray( "Tables" ).getJSONObject( 0 )
                                    .getJSONArray( "Fields" );
                        }
                        else
                        {
                            String strErrorMessage = jsonObject.getJSONObject( "object" ).getJSONObject( "ResultInfos" ).getString( "ErrorMessage" );
                            AppLogService.error( "Error Eudonet : " + strErrorMessage );
                        }
                    }

                }
            }
            catch( Exception ex )
            {
                AppLogService.error( "Erreur to generate the attribute list of eudonet", ex );
            }
        }

        return null;
    }

    public ReferenceList getTableList( JSONArray jsonArray )
    {
        ReferenceList referenceList = new ReferenceList( );

        if ( jsonArray != null )
        {
            for ( int i = 0; i < jsonArray.size( ); i++ )
            {
                ReferenceItem referenceItem = new ReferenceItem( );
                referenceItem.setCode( jsonArray.getJSONObject( i ).getString( "Descid" ) );
                referenceItem.setName( jsonArray.getJSONObject( i ).getString( "Label" ) );
                referenceList.add( referenceItem );
            }
        }

        return referenceList;
    }

    public ReferenceList getAttributList( JSONArray jsonArray )
    {
        ReferenceList referenceList = new ReferenceList( );

        if ( jsonArray != null )
        {
            for ( int i = 0; i < jsonArray.size( ); i++ )
            {
                ReferenceItem referenceItem = new ReferenceItem( );
                referenceItem.setCode( jsonArray.getJSONObject( i ).getString( "DescId" ) );
                referenceItem.setName( jsonArray.getJSONObject( i ).getString( "Label" ) );
                referenceList.add( referenceItem );
            }
        }

        return referenceList;
    }
}
