package open.dolphin.order;

/**
 *
 * @author Kazushi Minagawa
 */
public interface IInjectionView {
       
    /**
     * @return the clearBtn
     */
    public javax.swing.JButton getClearBtn();

    /**
     * @return the countField
     */
    public javax.swing.JTextField getCountField();

    /**
     * @return the deleteBtn
     */
    public javax.swing.JButton getDeleteBtn();

    /**
     * @return the infoLabel
     */
    public javax.swing.JLabel getInfoLabel();

    /**
     * @return the okBtn
     */
    public javax.swing.JButton getOkBtn();

    /**
     * @return the okCntBtn
     */
    public javax.swing.JButton getOkCntBtn();

    /**
     * @return the rtBtn
     */
    public javax.swing.JCheckBox getRtBtn();

    /**
     * @return the searchResultTabel
     */
    public javax.swing.JTable getSearchResultTable();

    /**
     * @return the searchTextField
     */
    public javax.swing.JTextField getSearchTextField();

    /**
     * @return the stampNameField
     */
    public javax.swing.JTextField getStampNameField();

    /**
     * @return the techChk
     */
    public javax.swing.JCheckBox getTechChk();

    /**
     * @return the setTable
     */
    public javax.swing.JTable getSetTable();

    public javax.swing.JCheckBox getNoChargeChk();

    public javax.swing.JCheckBox getPartialChk();

//s.oh^ 2014/03/31 スタンプ回数対応
    public javax.swing.JComboBox getNumberCombo();
//s.oh$
    
//s.oh^ 2014/10/22 Icon表示
    public javax.swing.JLabel getSearchLabel();
//s.oh$
    
}
