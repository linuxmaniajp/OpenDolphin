package open.dolphin.letter;

import java.awt.BorderLayout;
import java.awt.print.PageFormat;
import java.io.File;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import open.dolphin.client.AbstractChartDocument;
import open.dolphin.client.AutoKanjiListener;
import open.dolphin.client.BundleTransferHandler;
import open.dolphin.client.ClientContext;
import open.dolphin.client.CutCopyPasteAdapter;
import open.dolphin.client.Letter;
import open.dolphin.client.Panel2;
import open.dolphin.delegater.LetterDelegater;
import open.dolphin.helper.DBTask;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.LetterItem;
import open.dolphin.infomodel.LetterModule;
import open.dolphin.infomodel.LetterText;
import open.dolphin.infomodel.ModelUtils;
import open.dolphin.infomodel.PatientModel;
import open.dolphin.infomodel.UserModel;
import open.dolphin.project.Project;
import org.apache.log4j.Level;

/**
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class MedicalCertificateImpl extends AbstractChartDocument implements Letter {

    protected static final String TITLE = "診断書";
    protected static final String ITEM_DISEASE = "disease";
    protected static final String TEXT_INFORMED_CONTENT = "informedContent";

    protected LetterModule model;
    protected MedicalCertificateView view;
    private boolean listenerIsAdded;

    protected LetterStateMgr stateMgr;
    protected boolean DEBUG;

    /** Creates a new instance of LetterDocument */
    public MedicalCertificateImpl() {
        setTitle(TITLE);
        DEBUG = ClientContext.getBootLogger().getLevel() == Level.DEBUG ? true : false;
    }

    @Override
    public void modelToView(LetterModule m) {

        if (view == null) {
            view = new MedicalCertificateView();
        }

        // 患者氏名
        LetterHelper.setModelValue(view.getPatientNameFld(), m.getPatientName());

        // 患者生年月日
        String val = LetterHelper.getBirdayWithAge(m.getPatientBirthday(), m.getPatientAge());
        LetterHelper.setModelValue(view.getPatientBirthday(), val);

        // 患者住所
        LetterHelper.setModelValue(view.getPatientAddress(), m.getPatientAddress());

        // 日付
        String dateStr = LetterHelper.getDateAsString(m.getConfirmed());
        LetterHelper.setModelValue(view.getConfirmedFld(), dateStr);

        // 病院住所
        val = LetterHelper.getAddressWithZipCode(m.getConsultantAddress(), m.getConsultantZipCode());
        LetterHelper.setModelValue(view.getHospitalAddressFld(), val);

        // 病院名
        LetterHelper.setModelValue(view.getHospitalNameFld(), m.getConsultantHospital());

        // 医師
        LetterHelper.setModelValue(view.getDoctorNameFld(), m.getConsultantDoctor());

        //----------------------------------------------------------------------

        // 病名
        String value = model.getItemValue(ITEM_DISEASE);
        if (value != null) {
            LetterHelper.setModelValue(view.getDiseaseFld(), value);
        }

        // Informed
        String text = model.getTextValue(TEXT_INFORMED_CONTENT);
        if (text!=null) {
            LetterHelper.setModelValue(view.getInformedContent(), text);
        }
    }

    @Override
    public void viewToModel() {

        long savedId = model.getId();
        model.setId(0L);
        model.setLinkId(savedId);

        Date d = new Date();
        model.setConfirmed(d);
        model.setRecorded(d);
        model.setKarteBean(getContext().getKarte());
        model.setUserModel(Project.getUserModel());
        model.setStatus(IInfoModel.STATUS_FINAL);

        // 患者情報、差し出し人側はtartでmodelに設定済

        // 傷病名
        String value = LetterHelper.getFieldValue(view.getDiseaseFld());
        model.addLetterItem(new LetterItem(ITEM_DISEASE, value));

        // Informed
        String informed = LetterHelper.getAreaValue(view.getInformedContent());
        if (informed!=null) {
            LetterText text = new LetterText();
            text.setName(TEXT_INFORMED_CONTENT);
            text.setTextValue(informed);
            model.addLetterText(text);
        }

        // Title
        StringBuilder sb = new StringBuilder();
        sb.append(TITLE).append(":").append(value);
        model.setTitle(sb.toString());
    }

    @Override
    public void start() {

        this.model = new LetterModule();

        // Handle Class
        this.model.setHandleClass(MedicalCertificateViewer.class.getName());
        this.model.setLetterType(IInfoModel.MEDICAL_CERTIFICATE);

        // 確定日等
        Date d = new Date();
        this.model.setConfirmed(d);
        this.model.setRecorded(d);
        this.model.setStarted(d);
        this.model.setStatus(IInfoModel.STATUS_FINAL);
        this.model.setKarteBean(getContext().getKarte());
        this.model.setUserModel(Project.getUserModel());

        // 患者情報
        PatientModel patient = getContext().getPatient();
        this.model.setPatientId(patient.getPatientId());
        this.model.setPatientName(patient.getFullName());
        this.model.setPatientKana(patient.getKanaName());
        this.model.setPatientGender(patient.getGenderDesc());
        this.model.setPatientBirthday(patient.getBirthday());
        this.model.setPatientAge(ModelUtils.getAge(patient.getBirthday()));
        if (patient.getSimpleAddressModel()!=null) {
            this.model.setPatientAddress(patient.getSimpleAddressModel().getAddress());
        }
        this.model.setPatientTelephone(patient.getTelephone());

        // 病院
        UserModel user = Project.getUserModel();
        this.model.setConsultantHospital(user.getFacilityModel().getFacilityName());
        this.model.setConsultantDoctor(user.getCommonName());
        this.model.setConsultantDept(user.getDepartmentModel().getDepartmentDesc());
        this.model.setConsultantTelephone(user.getFacilityModel().getTelephone());
        this.model.setConsultantFax(user.getFacilityModel().getFacsimile());
        this.model.setConsultantZipCode(user.getFacilityModel().getZipCode());
        this.model.setConsultantAddress(user.getFacilityModel().getAddress());

        // view を生成
        this.view = new MedicalCertificateView();
        JScrollPane scroller = new JScrollPane(this.view);
        getUI().setLayout(new BorderLayout());
        getUI().add(scroller);

        modelToView(this.model);
        setEditables(true);
        setListeners();
        
        stateMgr = new LetterStateMgr(this);
    }

    @Override
    public void stop() {
    }

    @Override
    public void save() {

        viewToModel();

        DBTask task = new DBTask<Boolean, Void>(getContext()) {

            @Override
            protected Boolean doInBackground() throws Exception {

                LetterDelegater ddl = new LetterDelegater();
                long result = ddl.saveOrUpdateLetter(model);
                model.setId(result);
                return true;
            }

            @Override
            protected void succeeded(Boolean result) {
                getContext().getDocumentHistory().getDocumentHistory();
                stateMgr.processSavedEvent();
            }
        };

        task.execute();
    }

    @Override
    public void enter() {
        super.enter();
        if (stateMgr != null) {
            stateMgr.enter();
        }
    }

    @Override
    public void print() {
        
        if (this.model == null) {
            return;
        }
        
        viewToModel();

        StringBuilder sb = new StringBuilder();
        sb.append("PDFファイルを作成しますか?");

        int option = JOptionPane.showOptionDialog(
                getContext().getFrame(),
                sb.toString(),
                ClientContext.getFrameTitle("診断書印刷"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"PDF作成", "フォーム印刷", "取消し"},
                "PDF作成");

        if (option == 0) {
            makePDF();
        } else if (option == 1) {
            PageFormat pageFormat = getContext().getContext().getPageFormat();
            String name = getContext().getPatient().getFullName();
            Panel2 panel = (Panel2) this.view;
            panel.printPanel(pageFormat, 1, false, name, 0);
        }
    }

    @Override
    public void makePDF() {

        if (this.model == null) {
            return;
        }

        Runnable r = new Runnable() {

            @Override
            public void run() {

                MedicalCertificatePDFMaker pdf = new MedicalCertificatePDFMaker();
                String pdfDir = Project.getString("pdfStore", System.getProperty("user.dir"));
                pdf.setDocumentDir(pdfDir);
                pdf.setModel(model);
                boolean result = pdf.create();
                
                if (result) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(pdf.getDocumentDir());
                    sb.append(File.separator);
                    sb.append(pdf.getFileName());
                    String path = sb.toString();

                    try {
                        File target = new File(path);
                        if (target.exists()) {
                            if (ClientContext.isMac()) {
                                new ProcessBuilder("open", path).start();
                            } else if (ClientContext.isWin()) {
                                new ProcessBuilder("cmd.exe", "/c", path).start();
                            } else {
                                
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }
            }
        };
        Thread t = new Thread(r);
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    @Override
    public boolean isDirty() {
        if (stateMgr != null) {
            return stateMgr.isDirtyState();
        } else {
            return super.isDirty();
        }
    }

    public void modifyKarte() {
        stateMgr.processModifyKarteEvent();
    }

    @Override
    public void setEditables(boolean b) {
        view.getDiseaseFld().setEditable(b);
        view.getInformedContent().setEditable(b);
    }

    @Override
    public void setListeners() {

        if (listenerIsAdded) {
            return;
        }

        DocumentListener dl = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                stateMgr.processDirtyEvent();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                stateMgr.processDirtyEvent();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                stateMgr.processDirtyEvent();
            }
        };

        // 傷病名
        view.getDiseaseFld().getDocument().addDocumentListener(dl);
        view.getDiseaseFld().addFocusListener(AutoKanjiListener.getInstance());
        view.getDiseaseFld().setTransferHandler(new BundleTransferHandler());
        view.getDiseaseFld().addMouseListener(CutCopyPasteAdapter.getInstance());

        // Informed
        view.getInformedContent().getDocument().addDocumentListener(dl);
        view.getInformedContent().addFocusListener(AutoKanjiListener.getInstance());
        view.getInformedContent().setTransferHandler(new BundleTransferHandler());
        view.getInformedContent().addMouseListener(CutCopyPasteAdapter.getInstance());

        listenerIsAdded = true;
    }

    @Override
    public boolean letterIsDirty() {
        boolean dirty =  true;
        dirty = dirty && (LetterHelper.getFieldValue(view.getDiseaseFld()) != null);
        dirty = dirty && (LetterHelper.getAreaValue(view.getInformedContent()) != null);
        return dirty;
    }
}