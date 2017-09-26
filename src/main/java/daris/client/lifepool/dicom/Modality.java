package daris.client.lifepool.dicom;

public enum Modality {
    // @formatter:off
    AR("Autorefraction"),
    ASMT("Content Assessment Results"), 
    AU("Audio"), 
    BDUS("Bone Densitometry (ultrasound)"), 
    BI("Biomagnetic imaging"), 
    BMD("Bone Densitometry (X-Ray)"), 
    CR("Computed Radiography"), 
    CT("Computed Tomography"), 
    DG("Diaphanography"), 
    DOC("Document"), 
    DX("Digital Radiography"), 
    ECG("Electrocardiography"), 
    EPS("Cardiac Electrophysiology"), 
    ES("Endoscopy"), 
    FID("Fiducials"), 
    GM("General Microscopy"), 
    HC("Hard Copy"), 
    HD("Hemodynamic Waveform"), 
    IO("Intra-Oral Radiography"), 
    IOL("Intraocular Lens Data"), 
    IVOCT("Intravascular Optical Coherence Tomography"), 
    IVUS("Intravascular Ultrasound"), 
    KER("Keratometry"), 
    KO("Key Object Selection"), 
    LEN("Lensometry"), 
    LS("Laser surface scan"), 
    MG("Mammography"), 
    MR("Magnetic Resonance"), 
    NM("Nuclear Medicine"), 
    OAM("Ophthalmic Axial Measurements"), 
    OCT("Optical Coherence Tomography (non-Ophthalmic)"), 
    OP("Ophthalmic Photography"), 
    OPM("Ophthalmic Mapping"), 
    OPT("Ophthalmic Tomography"), 
    OPV("Ophthalmic Visual Field"), 
    OSS("Optical Surface Scan"), 
    OT("Other"), 
    PLAN("Plan"), 
    PR("Presentation State"), 
    PT("Positron emission tomography (PET)"), 
    PX("Panoramic X-Ray"), 
    REG("Registration"), 
    RESP("Respiratory Waveform"), 
    RF("Radio Fluoroscopy"), 
    RG("Radiographic imaging (conventional film/screen)"), 
    RTDOSE("Radiotherapy Dose"), 
    RTIMAGE("Radiotherapy Image"), 
    RTPLAN("Radiotherapy Plan"), 
    RTRECORD("RT Treatment Record"), 
    RTSTRUCT("Radiotherapy Structure Set"), 
    RMV("Real World Value Map"), 
    SEG("Segmentation"), 
    SM("Slide Microscopy"), 
    SMR("Stereometric Relationship"), 
    SR("Structured Report"), 
    SRF("Subjective Refraction"), 
    STAIN("Automated Slide Stainer"), 
    TG("Thermography"), 
    US("Ultrasound"), 
    VA("Visual Acuity"), 
    XA("X-Ray Angiography"), 
    XC("External-camera Photography");
    // @formatter:on
    private String _description;

    Modality(String description) {
        _description = description;
    }

    public String description() {
        return _description;
    }

    @Override
    public String toString() {
        return name();
    }

}
