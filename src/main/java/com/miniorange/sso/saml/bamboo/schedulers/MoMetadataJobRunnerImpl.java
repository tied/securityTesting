package com.miniorange.sso.saml.bamboo.schedulers;

import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;
import com.atlassian.scheduler.status.JobDetails;
import com.miniorange.sso.saml.bamboo.MoPluginConstants;
import com.miniorange.sso.saml.bamboo.MoPluginHandler;
import com.miniorange.sso.saml.bamboo.MoSAMLSettings;
import com.miniorange.sso.saml.dto.MoIDPConfig;
import com.miniorange.sso.saml.utils.MoHttpUtils;
import com.miniorange.sso.saml.utils.MoSAMLUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class MoMetadataJobRunnerImpl implements JobRunner, InitializingBean, DisposableBean{
	private static final Random RANDOM = new Random();
	private long SCHEDULE_BASE_INTERVAL_MILLIS = 120000;
	private static final int SCHEDULE_MAX_JITTER = 10000;

//	private static final JobRunnerKey JOB = JobRunnerKey.of(MoMetadataJobRunnerImpl.class.getName());
	private static final String JOB_ID_PREFIX = "Job ID =";
	public static final String KEY = MoMetadataJobRunnerImpl.class.getName()+":instance";
	public static final String JOB_NAME = MoMetadataJobRunnerImpl.class.getName()+":job";
	public static final String IDP_KEY = MoMetadataJobRunnerImpl.class.getName() + ":idp";

	private final SchedulerService schedulerService;
	private final MoSAMLSettings settings;

	public MoMetadataJobRunnerImpl(SchedulerService schedulerService, MoSAMLSettings settings) {
		super();
		this.schedulerService = schedulerService;
		this.settings = settings;
	}

	public void createSchedule(String jobID, long intervalInMillis) throws Exception {
		final int jitter = RANDOM.nextInt(SCHEDULE_MAX_JITTER);
		final Date firstRun = new Date(System.currentTimeMillis() + jitter);
		JobRunnerKey JOB = JobRunnerKey.of(MoMetadataJobRunnerImpl.class.getName()+jobID);
		final JobConfig jobConfig = JobConfig.forJobRunnerKey(JOB)
				.withSchedule(Schedule.forInterval(intervalInMillis, firstRun))
				.withRunMode(RunMode.RUN_ONCE_PER_CLUSTER);
		try {
			final JobId jobId = toJobId(jobID);
			final JobDetails existing = schedulerService.getJobDetails(jobId);
			if (existing != null) {
				schedulerService.unscheduleJob(jobId);
			}
			schedulerService.scheduleJob(jobId, jobConfig);
		} catch (SchedulerServiceException sse) {
			throw new Exception("Unable to create schedule for job ID '" + jobID + '\'', sse);
		}
	}

	public void deleteSchedule(String jobID) throws Exception {
		final JobId jobId = toJobId(jobID);
		JobRunnerKey JOB = JobRunnerKey.of(MoMetadataJobRunnerImpl.class.getName()+jobID);
		final JobDetails jobDetails = schedulerService.getJobDetails(jobId);
		if (jobDetails != null) {
			if (!(JOB).equals(jobDetails.getJobRunnerKey())) {
				throw new Exception("JobId '" + jobID + "' does not belong to me!");
			}
			schedulerService.unscheduleJob(jobId);
		}
	}

	private static JobId toJobId(String jobID) {
		return JobId.of(jobID);
	}

	@Override
	public JobRunnerResponse runJob(JobRunnerRequest request) {
		// TODO Auto-generated method stub
		try {
			String metadataUrl = "";
			String idpID = request.getJobId().toString();
			MoIDPConfig idpConfig = MoPluginHandler.constructIdpConfigObject(idpID.trim());
			if (idpConfig == null) {
				metadataUrl = settings.getIdpMetadataURL();
				if (StringUtils.isNotBlank(metadataUrl)) {
					if (settings.getRefreshMetadata() && StringUtils.isNotBlank(metadataUrl)) {
						String metadata = MoHttpUtils.sendGetRequest(metadataUrl);
						if (StringUtils.isNotBlank(metadata)) {
							MoPluginHandler.configureFromMetadata(MoPluginConstants.DEFAULT_IDP_ID, "IDP", metadata, settings.getRefreshMetadata());
						}
					}
				} else {
					deleteSchedule(MoPluginConstants.DEFAULT_IDP_ID);
				}
			} else {
				metadataUrl = idpConfig.getInputUrl();
				if (StringUtils.isNotBlank(metadataUrl)) {
					if (idpConfig.getRefreshMetadata() && StringUtils.isNotBlank(metadataUrl)) {
						String metadata = MoHttpUtils.sendGetRequest(metadataUrl);
						if (StringUtils.isNotBlank(metadata)) {
							MoPluginHandler.configureFromMetadata(idpID, idpConfig.getIdpName(), metadata, true);
						}
					}
				} else {
					deleteSchedule(idpID);
				}
			}


		} catch (Throwable t) {
			t.printStackTrace();
		}

		return null;
	}

	public void schedule(int interval, MoIDPConfig idpConfig) throws Exception{
		String jobID = idpConfig.getId();
		JobRunnerKey JOB = JobRunnerKey.of(MoMetadataJobRunnerImpl.class.getName()+jobID);
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MINUTE, interval);
		long refreshIntervalInMiliseconds = Integer.toUnsignedLong(interval*60*1000);
		schedulerService.registerJobRunner(JOB, this);
        createSchedule(idpConfig.getId(), refreshIntervalInMiliseconds);
	}
	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub
		JobRunnerKey JOB = JobRunnerKey.of(MoMetadataJobRunnerImpl.class.getName());
		schedulerService.unregisterJobRunner(JOB);
		deleteSchedule(MoPluginConstants.DEFAULT_IDP_ID);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		JobRunnerKey JOB = JobRunnerKey.of(MoMetadataJobRunnerImpl.class.getName());
		schedulerService.registerJobRunner(JOB, this);
        if (settings != null && StringUtils.isNotBlank(settings.getIdpMetadataURL()) && BooleanUtils.toBoolean
				(settings.getRefreshMetadata())) {
			int interval = MoSAMLUtils.getMetadataRefreshInterval(settings.getRefreshInterval(), settings
					.getCustomRefreshInterval(), settings.getCustomRefreshIntervalUnit());
			createSchedule(MoPluginConstants.DEFAULT_IDP_ID, interval);
		}
	}

	public SchedulerService getSchedulerService() {
		return schedulerService;
	}

	public MoSAMLSettings getSettings() {
		return settings;
	}

}
