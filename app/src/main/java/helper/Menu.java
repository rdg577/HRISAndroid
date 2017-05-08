package helper;

/**
 * Created by Reden Gallera on 14/03/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Menu {

    private int Id;
    private String Title;
    private String IconUrl;
    private int TotalApplications;

    public Menu(int id, String title, String iconUrl, int totalApplications) {
        this.Id = id;
        this.Title = title;
        this.IconUrl = iconUrl;
        this.TotalApplications = totalApplications;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getIconUrl() {
        return IconUrl;
    }

    public void setIconUrl(String iconUrl) {
        IconUrl = iconUrl;
    }

    public int getTotalApplications() {
        return TotalApplications;
    }

    public void setTotalApplications(int totalApplications) {
        TotalApplications = totalApplications;
    }
}
