package helper;

/**
 * Created by Reden Gallera on 14/03/2017.
 */

public class Menu {
    public int Id;
    public String Title;
    public String IconUrl;
    public int TotalApplications;

    public Menu(int id, String title, String iconUrl, int totalApplications) {
        this.Id = id;
        this.Title = title;
        this.IconUrl = iconUrl;
        this.TotalApplications = totalApplications;
    }
}
