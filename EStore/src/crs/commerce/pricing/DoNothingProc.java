package crs.commerce.pricing;

import atg.nucleus.GenericService;
import atg.service.pipeline.PipelineProcessor;
import atg.service.pipeline.PipelineResult;

public class DoNothingProc extends GenericService implements PipelineProcessor{

	private final int SUCCESS = 1;
	private final int[] RET_CODES = { 1, 2 };
	
	@Override
	public int[] getRetCodes() {
		return RET_CODES;
	}

	@Override
	public int runProcess(Object paramObject, PipelineResult paramPipelineResult)
			throws Exception {
		vlogDebug("Inside DoNothingProc.runProcess");
		return SUCCESS;
	}

}
