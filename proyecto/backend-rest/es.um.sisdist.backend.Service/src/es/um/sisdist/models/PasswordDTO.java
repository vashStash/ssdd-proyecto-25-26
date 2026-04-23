package es.um.sisdist.models;

public class PasswordDTO {
    private String newPassword;
    private String oldPassword;

    /**
     * @return the old password
     */
    public String getOldPassword()
    {
        return oldPassword;
    }

    /**
     * @return the newPassword
     */
    public String getNewPassword()
    {
        return newPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
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
