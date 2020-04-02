package edu.ucla.library.libservices.mailer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MassMailer
{
  private static final Logger logger = LogManager.getLogger(MassMailer.class);
  private static Properties props;
  private static BufferedReader reader;

  public MassMailer()
  {
    super();
  }

  public static void main(String[] args)
  {
    loadProperties(args[0]);
    openFile(args[1]);
    sendEmails();
    closeFile();
  }

  private static void loadProperties(String propFile)
  {
    props = new Properties();
    try
    {
      props.load(new FileInputStream(new File(propFile)));
    }
    catch (IOException ioe)
    {
      logger.fatal("problem with props file: " + ioe.getMessage());
      System.exit(-1);
    }
  }

  private static void openFile(String source)
  {
    try
    {
      reader = new BufferedReader(new FileReader(new File(source)));
    }
    catch (FileNotFoundException fnfe)
    {
      logger.fatal("problem opening emails file: " + fnfe.getMessage());
      System.exit(-2);
    }
  }

  private static void closeFile()
  {
    try
    {
      reader.close();
    }
    catch (IOException ioe)
    {
      logger.fatal("problem closing emails file: " + ioe.getMessage());
      System.exit(-3);
    }
  }

  private static void sendEmails()
  {
    Properties sysProps;
    Session session;
    MimeMessage msg;
    InternetAddress toAddress;
    String toMail;
    
    toMail = null;

    try
    {
      while ((toMail = reader.readLine()) != null)
      {
        sysProps = System.getProperties();
        sysProps.put( "mail.smtp.host", 
                      props.getProperty( "mailhost" ) );
        sysProps.put( "mail.mime.charset", "UTF-8" );
        session = Session.getInstance( sysProps, null );
        msg = new MimeMessage( session );
        try
        {
          msg.setFrom(new InternetAddress(props.getProperty("from")));
          toAddress = new InternetAddress( toMail );
          msg.setRecipient( Message.RecipientType.TO, toAddress );
          msg.setSubject( props.getProperty("subject") );
          msg.setHeader( "X-Mailer", 
                         props.getProperty("mailer") );
          msg.setSentDate( new Date() );
          msg.setContent(props.getProperty("body"), "text/html");
          Transport.send( msg );
        }
        catch (MessagingException me)
        {
          logger.error("problem generating email: " + me.getMessage());
        }
      }
    }
    catch (IOException ioe)
    {
      logger.error("problem reading emails file: " + ioe.getMessage());
    }
  }
}
