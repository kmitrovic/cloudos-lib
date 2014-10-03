package cloudos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cobbzilla.util.string.StringUtil;
import org.cobbzilla.wizard.model.UniquelyNamedEntity;
import org.cobbzilla.wizard.validation.HasValue;
import org.hibernate.validator.constraints.Email;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

@MappedSuperclass @Accessors(chain=true)
public class AccountBase extends UniquelyNamedEntity {

    public static final String ERR_AUTHID_LENGTH = "{err.authid.length}";
    public static final String ERR_EMAIL_INVALID = "{err.email.invalid}";
    public static final String ERR_EMAIL_EMPTY = "{err.email.empty}";
    public static final String ERR_EMAIL_LENGTH = "{err.email.length}";
    public static final String ERR_LAST_NAME_EMPTY = "{err.lastName.empty}";
    public static final String ERR_LAST_NAME_LENGTH = "{err.lastName.length}";
    public static final String ERR_FIRST_NAME_EMPTY = "{err.firstName.empty}";
    public static final String ERR_FIRST_NAME_LENGTH = "{err.firstName.length}";
    public static final String ERR_MOBILEPHONE_LENGTH = "{err.mobilePhone.length}";
    public static final String ERR_MOBILEPHONE_EMPTY = "{err.mobilePhone.empty}";
    public static final String ERR_MOBILEPHONE_CC_EMPTY = "{err.mobilePhoneCountryCode.empty}";
    public static final String ERR_PRIMARY_GROUP_LENGTH = "{err.primaryGroup.length}";
    public static final int EMAIL_MAXLEN = 255;
    public static final int VERIFY_CODE_MAXLEN = 100;
    public static final int LASTNAME_MAXLEN = 25;
    public static final int FIRSTNAME_MAXLEN = 25;
    public static final int MOBILEPHONE_MAXLEN = 30;
    public static final int PRIMARY_GROUP_MAXLEN = 100;

    @Size(max=30, message=ERR_AUTHID_LENGTH)
    @Getter @Setter private String authId = null;

    public boolean hasAuthId() { return !StringUtil.empty(authId); }

    @JsonIgnore @Transient public Integer getAuthIdInt() { return authId == null ? null : Integer.valueOf(authId); }
    public AccountBase setAuthIdInt(int authId) { setAuthId(String.valueOf(authId)); return this; }

    @Transient
    public String getAccountName () { return getName(); }
    public AccountBase setAccountName (String name) { setName(name); return this; }

    @HasValue(message=ERR_LAST_NAME_EMPTY)
    @Size(max=LASTNAME_MAXLEN, message=ERR_LAST_NAME_LENGTH)
    @Column(nullable=false, length=LASTNAME_MAXLEN)
    @Getter @Setter private String lastName;

    @HasValue(message=ERR_FIRST_NAME_EMPTY)
    @Size(max=FIRSTNAME_MAXLEN, message=ERR_FIRST_NAME_LENGTH)
    @Column(nullable=false, length=FIRSTNAME_MAXLEN)
    @Getter @Setter private String firstName;

    @Getter @Setter private boolean admin = false;
    @Getter @Setter private boolean suspended = false;
    @Getter @Setter private boolean twoFactor = false;
    @Getter @Setter private Long lastLogin = null;
    public AccountBase setLastLogin () { lastLogin = System.currentTimeMillis(); return this; }

    @Email(message=ERR_EMAIL_INVALID)
    @HasValue(message=ERR_EMAIL_EMPTY)
    @Size(max=EMAIL_MAXLEN, message=ERR_EMAIL_LENGTH)
    @Column(unique=true, nullable=false, length=EMAIL_MAXLEN)
    @Getter private String email;

    @JsonIgnore @Getter @Setter private String emailVerificationCode;
    @JsonIgnore @Size(max=VERIFY_CODE_MAXLEN) @Getter @Setter private Long emailVerificationCodeCreatedAt;
    @Getter @Setter private boolean emailVerified = false;

    public AccountBase setEmail (String email) {
        if (this.email == null || !this.email.equals(email)) {
            emailVerified = false;
            emailVerificationCode = null;
            emailVerificationCodeCreatedAt = null;
            this.email = email;
        }
        return this;
    }

    @Size(max=MOBILEPHONE_MAXLEN, message=ERR_MOBILEPHONE_LENGTH)
    @HasValue(message=ERR_MOBILEPHONE_EMPTY)
    @Getter private String mobilePhone;

    @JsonIgnore @Size(max=VERIFY_CODE_MAXLEN) @Getter @Setter private String mobilePhoneVerificationCode;
    @JsonIgnore @Getter @Setter private Long mobilePhoneVerificationCodeCreatedAt;
    @Getter @Setter private boolean mobilePhoneVerified = false;

    public AccountBase setMobilePhone (String mobilePhone) {
        if (this.mobilePhone == null || !this.mobilePhone.equals(mobilePhone)) {
            mobilePhoneVerified = false;
            mobilePhoneVerificationCode = null;
            mobilePhoneVerificationCodeCreatedAt = null;
            this.mobilePhone = mobilePhone;
        }
        return this;
    }

    @HasValue(message= ERR_MOBILEPHONE_CC_EMPTY)
    @Getter private Integer mobilePhoneCountryCode;

    public AccountBase setMobilePhoneCountryCode(Integer mobilePhoneCountryCode) {
        if (this.mobilePhoneCountryCode == null || !this.mobilePhoneCountryCode.equals(mobilePhoneCountryCode)) {
            mobilePhoneVerified = false;
            mobilePhoneVerificationCode = null;
            mobilePhoneVerificationCodeCreatedAt = null;
            this.mobilePhoneCountryCode = mobilePhoneCountryCode;
        }
        return this;
    }

    @JsonIgnore @Transient public String getMobilePhoneCountryCodeString() { return mobilePhoneCountryCode == null ? null : mobilePhoneCountryCode.toString(); }

}
