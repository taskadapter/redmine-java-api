package com.taskadapter.redmineapi.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Wiki
    implements Identifiable, Serializable
{

    private static final long serialVersionUID = 1L;

    private Integer id;
    
    private String identifier;

    private User user;

    private String title;

    private String text;

    private Date createdOn;

    private Project project;
    
    private Wiki parent;
    
    private List<Attachment> attachments = new ArrayList<Attachment>();

    public Wiki()
    {
        super();
    }

    public Integer getId()
    {
        return id;
    }

    public void setId( Integer aId )
    {
        this.id = aId;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser( User aUser )
    {
        this.user = aUser;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle( String aTitle )
    {
        this.title = aTitle;
    }

    public String getText()
    {
        return text;
    }

    public void setText( String text )
    {
        this.text = text;
    }

    public Date getCreatedOn()
    {
        return createdOn;
    }

    public void setCreatedOn( Date aCreated )
    {
        this.createdOn = aCreated;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( createdOn == null ) ? 0 : createdOn.hashCode() );
        result = prime * result + ( ( text == null ) ? 0 : text.hashCode() );
        result = prime * result + ( ( user == null ) ? 0 : user.hashCode() );
        result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
        result = prime * result + ( ( title == null ) ? 0 : title.hashCode() );

        return result;
    }
    
    @Override
    public String toString() {
        return "Wiki [id=" + id + ", title=" + title + "]";
    }

    public Project getProject()
    {
        return project;
    }

    public void setProject( Project project )
    {
        this.project = project;
    }

    public String getIdentifier()
    {
        if ( this.identifier == null )  return this.getTitle().replace( " ", "_" );
        return identifier;
    }

    public void setIdentifier( String identifier )
    {
        this.identifier = identifier;
    }
    
    public List<Attachment> getAttachments() {
        return attachments;
    }
    
    public void addAttachment(Attachment attachment){
        attachments.add( attachment );
    }
    
    public void setAttachments(List<Attachment> attachments){
        this.attachments = attachments;
    }

    public void setParent( Wiki parent )
    {
        this.parent = parent;
    }
    
    public Wiki getParent()
    {
        return parent;
    }

}
