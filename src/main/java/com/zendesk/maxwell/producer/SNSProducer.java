package com.zendesk.maxwell.producer;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.zendesk.maxwell.MaxwellContext;
import com.zendesk.maxwell.RowMap;

public class SNSProducer extends AbstractProducer {

	AmazonSNSClient snsClient;
	String topicURL;

	public SNSProducer(MaxwellContext context) {
		super(context);

		this.snsClient = new AmazonSNSClient(
				new ClasspathPropertiesFileCredentialsProvider("~/.aws/credentials"));
		this.snsClient.setRegion(Region.getRegion(Regions.US_WEST_2));
	}

	@Override
	public void push(RowMap r) throws Exception {
		try {
			
			// publish to an SNS topic
			PublishRequest publishRequest = new PublishRequest(topicURL, r.toJSON());
			snsClient.publish(publishRequest);
			this.context.setPosition(r);
			
		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon SQS, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with SNS, such as not "
					+ "being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}
}
