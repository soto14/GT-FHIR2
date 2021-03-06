package edu.gatech.chai.gtfhir2.provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.hl7.fhir.dstu3.model.ContactPoint.ContactPointUse;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.InstantType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.Create;
//import ca.uhn.fhir.model.dstu2.composite.ContactPointDt;
//import ca.uhn.fhir.model.dstu2.valueset.ContactPointUseEnum;
//import ca.uhn.fhir.model.primitive.CodeDt;
//import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import edu.gatech.chai.gtfhir2.mapping.OmopOrganization;
import edu.gatech.chai.gtfhir2.model.MyOrganization;
import edu.gatech.chai.omopv5.jpa.service.ParameterWrapper;

/**
 * This is a simple resource provider which only implements "read/GET" methods,
 * but which uses a custom subclassed resource definition to add statically
 * bound extensions.
 * 
 * See the MyOrganization definition to see how the custom resource definition
 * works.
 */
public class OrganizationResourceProvider implements IResourceProvider {
	// private CareSiteService careSiteService;
	private WebApplicationContext myAppCtx;
	private String myDbType;
	private OmopOrganization myMapper;
	private int preferredPageSize = 30;

	public OrganizationResourceProvider() {
		myAppCtx = ContextLoaderListener.getCurrentWebApplicationContext();
		myDbType = myAppCtx.getServletContext().getInitParameter("backendDbType");
		if (myDbType.equalsIgnoreCase("omopv5") == true) {
			myMapper = new OmopOrganization(myAppCtx);
		} else {
			myMapper = new OmopOrganization(myAppCtx);
		}

		String pageSizeStr = myAppCtx.getServletContext().getInitParameter("preferredPageSize");
		if (pageSizeStr != null && pageSizeStr.isEmpty() == false) {
			int pageSize = Integer.parseInt(pageSizeStr);
			if (pageSize > 0) {
				preferredPageSize = pageSize;
			}
		}

	}

	/**
	 * The "@Create" annotation indicates that this method implements
	 * "create=type", which adds a new instance of a resource to the server.
	 */
	@Create()
	public MethodOutcome createPatient(@ResourceParam MyOrganization theOrganization) {
		// validateResource(thePatient);

		Long id = myMapper.toDbase(theOrganization, null);
		return new MethodOutcome(new IdDt(id));
	}

	/**
	 * The getResourceType method comes from IResourceProvider, and must be
	 * overridden to indicate what type of resource this provider supplies.
	 */
	@Override
	public Class<MyOrganization> getResourceType() {
		return MyOrganization.class;
	}

	/**
	 * The "@Read" annotation indicates that this method supports the read
	 * operation. It takes one argument, the Resource type being returned.
	 * 
	 * @param theId
	 *            The read operation takes one parameter, which must be of type
	 *            IdDt and must be annotated with the "@Read.IdParam"
	 *            annotation.
	 * @return Returns a resource matching this identifier, or null if none
	 *         exists.
	 */
	@Read()
	public MyOrganization getResourceById(@IdParam IdType theId) {
		MyOrganization retVal = (MyOrganization) myMapper.toFHIR(theId);
		if (retVal == null) {
			throw new ResourceNotFoundException(theId);
		}

		return retVal;
	}

	@Search()
	public IBundleProvider findOrganizationByParams(
			@OptionalParam(name = Patient.SP_RES_ID) TokenParam thePatientId,
			@OptionalParam(name = MyOrganization.SP_NAME) StringParam theName,
			
			@IncludeParam(allow={"Organization:partof"})
			final Set<Include> theIncludes
			) {
		final InstantType searchTime = InstantType.withCurrentTime();
		Map<String, List<ParameterWrapper>> paramMap = new HashMap<String, List<ParameterWrapper>> ();

		if (thePatientId != null) {
			mapParameter (paramMap, MyOrganization.SP_RES_ID, thePatientId);
		}
		if (theName != null) {
			mapParameter (paramMap, MyOrganization.SP_NAME, theName);
		}
		
		// Now finalize the parameter map.
		final Map<String, List<ParameterWrapper>> finalParamMap = paramMap;
		final Long totalSize;
		if (paramMap.size() == 0) {
			totalSize = myMapper.getSize();
		} else {
			totalSize = myMapper.getSize(finalParamMap);
		}

		return new IBundleProvider() {

			@Override
			public IPrimitiveType<Date> getPublished() {
				return searchTime;
			}

			@Override
			public List<IBaseResource> getResources(int fromIndex, int toIndex) {
				List<IBaseResource> retv = new ArrayList<IBaseResource>();

				// _Include
				List<String> includes = new ArrayList<String>();
				if (theIncludes.contains(new Include("Organization:partof"))) {
					includes.add("Organization:partof");
				}

				if (finalParamMap.size() == 0) {
					myMapper.searchWithoutParams(fromIndex, toIndex, retv, includes);
				} else {
					myMapper.searchWithParams(fromIndex, toIndex, finalParamMap, retv, includes);
				}

				return retv;
			}

			@Override
			public String getUuid() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Integer preferredPageSize() {
				return preferredPageSize;
			}

			@Override
			public Integer size() {
				return totalSize.intValue();
			}};
	}
	
	private void mapParameter(Map<String, List<ParameterWrapper>> paramMap, String FHIRparam, Object value) {
		List<ParameterWrapper> paramList = myMapper.mapParameter(FHIRparam, value);
		if (paramList != null) {
			paramMap.put(FHIRparam, paramList);
		}
	}

	/**
	 * This is the "read" operation. The "@Read" annotation indicates that this method supports the read and/or vread operation.
	 * <p>
	 * Read operations take a single parameter annotated with the {@link IdParam} paramater, and should return a single resource instance.
	 * </p>
	 * 
	 * @param theId
	 *            The read operation takes one parameter, which must be of type IdDt and must be annotated with the "@Read.IdParam" annotation.
	 * @return Returns a resource matching this identifier, or null if none exists.
	 */
	@Read()
	public MyOrganization readOrganization(@IdParam IdType theId) {
		MyOrganization retval = (MyOrganization) myMapper.toFHIR(theId);
		if (retval == null) {
			throw new ResourceNotFoundException(theId);
		}
			
		return retval;
	}

	/**
	 * The "@Update" annotation indicates that this method supports replacing an existing 
	 * resource (by ID) with a new instance of that resource.
	 * 
	 * @param theId
	 *            This is the ID of the patient to update
	 * @param thePatient
	 *            This is the actual resource to save
	 * @return This method returns a "MethodOutcome"
	 */
	@Update()
	public MethodOutcome updateOrganization(@IdParam IdType theId, @ResourceParam MyOrganization theOrganization) {
		validateResource(theOrganization);

		Long fhirId = myMapper.toDbase(theOrganization, theId);
		if (fhirId == null) {
			throw new ResourceNotFoundException(theId);
		}

		return new MethodOutcome();
	}
	
	// TODO: Add more validation code here.
	private void validateResource(MyOrganization theOrganization) {
	}


}
