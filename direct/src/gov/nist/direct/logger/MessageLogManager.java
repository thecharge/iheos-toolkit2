/**
 This software was developed at the National Institute of Standards and Technology by employees
of the Federal Government in the course of their official duties. Pursuant to title 17 Section 105 of the
United States Code this software is not subject to copyright protection and is in the public domain.
This is an experimental system. NIST assumes no responsibility whatsoever for its use by other parties,
and makes no guarantees, expressed or implied, about its quality, reliability, or any other characteristic.
We would appreciate acknowledgement if the software is used. This software can be redistributed and/or
modified freely provided that any derivative works bear some notice that they are derived from it, and any
modified versions bear some notice that they have been modified.

Project: NWHIN-DIRECT
Authors: William Majurski
		 Frederic de Vaulx
		 Diane Azais
		 Julien Perugini
		 Antoine Gerardin
		
 */

package gov.nist.direct.logger;

import gov.nist.direct.client.MessageLog;
import gov.nist.direct.logger.reader.DirectLogReader;
import gov.nist.direct.logger.writer.DirectContentLogger;
import gov.nist.direct.logger.writer.LabelLogger;
import gov.nist.direct.logger.writer.MessageIDLogger;
import gov.nist.direct.logger.writer.MessageStatusLogger;
import gov.nist.direct.logger.writer.TimeLogger;
import gov.nist.direct.logger.writer.messageLoggerImpl.MDNLogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.mail.internet.MimeMessage;

public class MessageLogManager {
	private static String UNKNOWN_USERNAME = "Unknown username";

	
	// general attributes
	private String label;

	MessageLog msgLog;
	private String transactionType; // DirectSend or DirectReceive
	private String messageType; // Direct or MDN

	// attributes relevant to the Direct message sent
	private String messageId;
	private Date expirationDate; // delay after which MDN is considered as arriving too late


	// MDN message, received
	private Date mdnReceivedDate;
	private String status;
	private LogPathsSingleton ls;
		

	/**
	 * Stores a single message log
	 * @param _testReference
	 * @param _transactionType
	 * @param _messageType
	 * @param _messageId
	 * @param _expirationDate
	 * @param _mdnReceivedDate
	 * @param _status
	 * @param _label 
	 * @param _ls
	 */
	public MessageLogManager(MessageLog msgLog){
		this.msgLog = msgLog;
	}
	
	/**
	 * Completes a Direct message log with matching MDN logs
	 * @param messageId
	 */
	public static void logMDN(MimeMessage m, String mdnValidationStatus, String origDirectMsgValidationStatus, String transactionType, String messageType, String origMessageId, Date receivedDate, String mdnMessageId){
		
		// find the username that matches the original message ID
		String username = "";
		if (findUsername(origMessageId) != ""){
			  username = findUsername(origMessageId);
			  System.out.println("When logging an MDN, username should not be empty.");
		}
		System.out.println("mdn username :" + username);
		
		
		// Log MDN message-ID
		MessageIDLogger idl = new MessageIDLogger();
		try {
			idl.logMessageId(mdnMessageId, transactionType, messageType, username, origMessageId);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		// Log MDN validation status
		MessageStatusLogger dl = new MessageStatusLogger();
		try {
			dl.logMDNValidationStatus(mdnValidationStatus, transactionType, messageType, username, origMessageId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Log validation status of the original Direct message (MDN ack value)
		try {
			dl.logDirectOriginalValidationStatus(origDirectMsgValidationStatus, transactionType, messageType, username, origMessageId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Log MDN received date
		TimeLogger tl = new TimeLogger();
		try {
			tl.logMDNReceivedDate(receivedDate, transactionType, messageType, username, origMessageId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Log full MDN message
		MDNLogger mdnlog = new MDNLogger();
		try {
			mdnlog.log(m, transactionType, messageType, username, origMessageId);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}
	
	/**
	 * Finds a username in the logging structure, based on its matching message-id
	 * @param origMsgId the original message-id (the Direct message=ID) that is extracted from a received MDN
	 * @return
	 */
	private static String findUsername(String origMsgId) {
		LogPathsSingleton ls = LogPathsSingleton.getLogStructureSingleton();
		
		String logRoot = LogPathsSingleton.getLOG_ROOT();
		List<String> usernames = LoggerUtils.listFilesForFolder("");
		
		String name = "";
		String msgIdFolder = logRoot + name + ls.getDIRECT_SEND_FOLDER() + origMsgId;
		File f;
		for (int i = 0; i < usernames.size() ; i++){
			name = usernames.get(i);
			System.out.println("msgidfolder " + msgIdFolder);
			f = new File(msgIdFolder);
			if (f.exists()) return name;
		}
	System.out.println("Error: No username matching original message ID "+ origMsgId +" could be found.");
		return UNKNOWN_USERNAME;
		
	}


	public static void logDirectMessage(String username, Date directMsgDateSent, String transactionType, String messageType, String messageId, MimeMessage directMessage, String label){
		// Log Direct message sent date
		TimeLogger tl = new TimeLogger();
		try {
			tl.logDirectReceivedDate(directMsgDateSent, transactionType, messageType, username, messageId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// log expiration date
		LogPathsSingleton ls = LogPathsSingleton.getLogStructureSingleton();
		ls.getDateExpirationLogPath(transactionType, messageType, username, messageId);
		Calendar cal = Calendar.getInstance(); // creates calendar
		cal.setTime(new Date()); // sets calendar time/date
		cal.add(Calendar.HOUR_OF_DAY, 1); // adds one hour
		Date expirationDate =  cal.getTime(); // returns new date object, one hour in the future
		try {
			tl.logExpirationDate(expirationDate, transactionType, messageType, username, messageId);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Log full Direct Message
		DirectContentLogger dcl = new DirectContentLogger();
		try {
			dcl.logMessageContents(directMessage, ls, transactionType, messageType, username, messageId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		// Log Label
		LabelLogger ll = new LabelLogger();
		try {
			ll.logLabel(label, transactionType, messageType, username, messageId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Log temporary status "MDN not received yet"
		String status =  "Waiting for MDN";
				MessageStatusLogger dl = new MessageStatusLogger();
				try {
					dl.logMDNValidationStatus( "Waiting for MDN", transactionType, messageType, username, messageId);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}


	/**
	 * Reads a single message log (Direct message + possible MDN acknowledgement + statuses and expiration dates)
	 * 
	 * No message type (direct / mdn) because a log includes both the Direct message sent and its MDN counterpart
	 * @param username
	 * @param transactionType
	 * @param messageId
	 * @return
	 */
	public static MessageLog readLog(String username, String _transactionType, String messageId){
		MessageLogManager log;
		String messageType;
		LogPathsSingleton ls = LogPathsSingleton.getLogStructureSingleton();

		DirectLogReader reader = new DirectLogReader();

		// **** parse folder Direct ****
		messageType =	ls.getDIRECT_MESSAGE_FOLDER();

		// read whole message content - should get path only
		//MimeMessage directContents = reader.readDirectMessage(ls, transactionType,  messageType, username, messageId);

		// read MDN validation status
			// reading MDN status first in case it exists
			//String	status = reader.readMessageStatus(ls, _transactionType,  ls.getMDN_MESSAGE_FOLDER(), username, messageId);
		//	if (status == null){
				// then read direct status
			String	status = reader.readMessageStatus(ls, _transactionType,  messageType, username, messageId);
			//}
		System.out.println("status " + status);
		
		
		// read label
		String label = reader.readLabel(ls, _transactionType,  messageType, username, messageId);
		
		// read sent date
		String directSendDate = reader.readDirectSendDate(ls, _transactionType, messageType, username, messageId);
		
		// read projected expiration date
		String expirationDate = reader.readMDNExpirationDate(ls, _transactionType, messageType, username, messageId);

		

		// **** parse folder MDN ****
		messageType =	ls.getMDN_MESSAGE_FOLDER();

		// read MDN actual receive date
		String mdnReceivedDate = reader.readMDNReceivedDate(ls, _transactionType, messageType, username, messageId);

		// read MDN message-ID
		String mdnMessageID = reader.readMDNMessageID(ls, _transactionType, messageType, username, messageId);
		

		// read original Direct message status (whether MDN indicates if the Direct msg is valid or not)
		String origDirectMsgStatus = reader.readOrigDirectMessageStatus(ls, _transactionType, messageType, username, messageId);
		
		
		// Get Transaction Type name as a String suitable for display
		String transactionLabel = "";
		if (_transactionType == ls.getDIRECT_SEND_FOLDER()) {
			transactionLabel = ls.getDIRECT_SEND_LABEL_FOR_DISPLAY();
		} 
		else if  (_transactionType == ls.getDIRECT_RECEIVE_FOLDER()) {
			transactionLabel = ls.getDIRECT_RECEIVE_LABEL_FOR_DISPLAY();
		} else {
			System.out.println("Transaction name unknown.");
		}
		
		// Get the Message Type as a String suitable for display
		String messageTypeLabel = "";
		if (messageType == ls.getDIRECT_MESSAGE_FOLDER()) {
			messageTypeLabel = ls.getDIRECT_MESSAGE_LABEL();
		} 
		else if  (messageType == ls.getMDN_MESSAGE_FOLDER()) {
			messageTypeLabel = ls.getMDN_MESSAGE_LABEL();
		} else {
			System.out.println("Message type unknown.");
		}
		
		return new MessageLog(transactionLabel, messageTypeLabel, messageId, directSendDate, expirationDate, mdnReceivedDate, mdnMessageID, status, origDirectMsgStatus, label);	
	}



}