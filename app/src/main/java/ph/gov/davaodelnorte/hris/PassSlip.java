package ph.gov.davaodelnorte.hris;

/**
 * Created by Reden Gallera on 10/03/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
class PassSlip {

    private int _recNo;
    private String _controlNo;
    private String _EIC;
    private String _fullname;
    private String _timeOut;
    private String _timeIn;
    private String _destination;
    private String _purpose;
    private int _isOfficial;
    private int _statusID;
    private String _apprvEIC;

    public String get_fullname() {
        return _fullname;
    }

    public void set_fullname(String _fullname) {
        this._fullname = _fullname;
    }

    public String get_controlNo() {
        return _controlNo;
    }

    public void set_controlNo(String _controlNo) {
        this._controlNo = _controlNo;
    }

    public int get_recNo() {

        return _recNo;
    }

    public void set_recNo(int _recNo) {
        this._recNo = _recNo;
    }

    public String get_EIC() {
        return _EIC;
    }

    public void set_EIC(String _EIC) {
        this._EIC = _EIC;
    }

    public String get_apprvEIC() {
        return _apprvEIC;
    }

    public void set_apprvEIC(String _apprvEIC) {
        this._apprvEIC = _apprvEIC;
    }

    public int get_statusID() {

        return _statusID;
    }

    public void set_statusID(int _statusID) {
        this._statusID = _statusID;
    }

    public int get_isOfficial() {

        return _isOfficial;
    }

    public void set_isOfficial(int _isOfficial) {
        this._isOfficial = _isOfficial;
    }

    public String get_purpose() {

        return _purpose;
    }

    public void set_purpose(String _purpose) {
        this._purpose = _purpose;
    }

    public String get_destination() {

        return _destination;
    }

    public void set_destination(String _destination) {
        this._destination = _destination;
    }

    public String get_timeIn() {

        return _timeIn;
    }

    public void set_timeIn(String _timeIn) {
        this._timeIn = _timeIn;
    }

    public String get_timeOut() {

        return _timeOut;
    }

    public void set_timeOut(String _timeOut) {
        this._timeOut = _timeOut;
    }
}
