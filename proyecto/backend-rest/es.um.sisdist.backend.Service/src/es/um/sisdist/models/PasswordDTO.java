package es.um.sisdist.models;

public class PasswordDTO {
    private String newPassword;
    private String oldPassword;

    /**
     * @return the old password
     */
    public String getOldPassowrd()
    {
        return oldPassword;
    }

    /**
     * @return the email
     */
    public String getNewPassword()
    {
        return newPassword;
    }

    public PasswordDTO(String newPwd, String oldPwd)
    {
        super();
        this.newPassword = newPwd;
        this.oldPassword = oldPwd;
    }

    @Override
    public String toString() {
        return "PasswordDTO [oldPassword=" + oldPassword + ", newPassword=" + newPassword +"]";
    }

    public PasswordDTO()
    {
    }
}
