db = db.getSiblingDB("selcOnboarding");

db.onboardings.insertMany([
  {
    _id: "89ad7142-24bb-48ad-8504-9c9231137i103",
    createdAt: ISODate("2024-10-18T12:24:50.050Z"),
    institution: {
      address: "sede leg",
      city: "Milano",
      country: "IT",
      county: "MI",
      description: "Techpartner 23-02",
      digitalAddress: "pec@pectest.com",
      geographicTaxonomies: [],
      id: "c2808e95-59a7-44fb-a6df-64faf4ff3ed3",
      imported: false,
      institutionType: "PT",
      origin: "INFOCAMERE",
      taxCode: "11223345665",
      zipCode: "11223",
    },
    productId: "prod-io",
    status: "PENDING",
    users: [
      {
        id: "ec7ca4d5-c537-462a-8c06-e102e0c21c44",
        role: "MANAGER",
        userMailUuid: "ID_MAIL#49e16aa5-bc28-40c7-9844-6886e4647e22",
      },
    ],
    workflowType: "USERS",
  },
  {
    _id: "89ad7142-24bb-48ad-8504-9c9231137i105",
    createdAt: ISODate("2024-10-18T12:24:50.050Z"),
    institution: {
      address: "sede leg",
      city: "Milano",
      country: "IT",
      county: "MI",
      description: "Techpartner 23-02",
      digitalAddress: "pec@pectest.com",
      geographicTaxonomies: [],
      id: "c2808e95-59a7-44fb-a6df-64faf4ff3ed3",
      imported: false,
      institutionType: "PT",
      origin: "INFOCAMERE",
      taxCode: "11223345665",
      zipCode: "11223",
    },
    productId: "prod-io",
    status: "PENDING",
    users: [
      {
        id: "ec7ca4d5-c537-462a-8c06-e102e0c21c44",
        role: "MANAGER",
        userMailUuid: "ID_MAIL#49e16aa5-bc28-40c7-9844-6886e4647e22",
      },
    ],
    workflowType: "USERS",
    referenceOnboardingId: "89ad7142-24bb-48ad-8504-9c9231137i110",
  },
  {
    _id: "89ad7142-24bb-48ad-8504-9c9231137i110",
    createdAt: ISODate("2024-10-18T12:24:50.050Z"),
    institution: {
      address: "sede leg",
      city: "Milano",
      country: "IT",
      county: "MI",
      description: "Techpartner 23-02",
      digitalAddress: "pec@pectest.com",
      geographicTaxonomies: [],
      id: "c2808e95-59a7-44fb-a6df-64faf4ff3ed3",
      imported: false,
      institutionType: "PT",
      origin: "INFOCAMERE",
      taxCode: "11223345667",
      zipCode: "11223",
    },
    productId: "prod-io",
    status: "COMPLETED",
    users: [
      {
        id: "ec7ca4d5-c537-462a-8c06-e102e0c21c44",
        role: "MANAGER",
        userMailUuid: "ID_MAIL#49e16aa5-bc28-40c7-9844-6886e4647e22",
      },
    ],
    workflowType: "CONTRACT_REGISTRATION",
  },
  {
    _id: "89ad7142-24bb-48ad-8504-9c9231137i122",
    createdAt: ISODate("2024-10-18T12:24:50.050Z"),
    institution: {
      address: "sede leg",
      city: "Milano",
      country: "IT",
      county: "MI",
      description: "Techpartner 23-02",
      digitalAddress: "pec@pectest.com",
      geographicTaxonomies: [],
      id: "c2808e95-59a7-44fb-a6df-64faf4ff3ed3",
      imported: false,
      institutionType: "PT",
      origin: "INFOCAMERE",
      taxCode: "11223345661",
      zipCode: "11223",
    },
    productId: "prod-io",
    status: "COMPLETED",
    users: [
      {
        id: "ec7ca4d5-c537-462a-8c06-e102e0c21c44",
        role: "MANAGER",
        userMailUuid: "ID_MAIL#49e16aa5-bc28-40c7-9844-6886e4647e22",
      },
    ],
    workflowType: "CONTRACT_REGISTRATION",
  },
  {
    _id: "89ad7142-24bb-48ad-8504-9c9231137i1022",
    createdAt: ISODate("2024-10-18T12:24:50.050Z"),
    institution: {
      address: "sede leg",
      city: "Milano",
      country: "IT",
      county: "MI",
      description: "Techpartner 23-02",
      digitalAddress: "pec@pectest.com",
      geographicTaxonomies: [],
      id: "c2808e95-59a7-44fb-a6df-64faf4ff3ed3",
      imported: false,
      institutionType: "PT",
      origin: "INFOCAMERE",
      taxCode: "11223345661",
      zipCode: "11223",
    },
    productId: "prod-io",
    status: "TOBEVALIDATED",
    users: [
      {
        id: "ec7ca4d5-c537-462a-8c06-e102e0c21c44",
        role: "MANAGER",
        userMailUuid: "ID_MAIL#49e16aa5-bc28-40c7-9844-6886e4647e22",
      },
    ],
    workflowType: "FOR_APPROVE",
  },
  {
    _id: "89ad7142-24bb-48ad-8504-9c9231137i123",
    createdAt: ISODate("2024-10-18T12:24:50.050Z"),
    institution: {
      address: "sede leg",
      city: "Milano",
      country: "IT",
      county: "MI",
      description: "Techpartner 23-02",
      digitalAddress: "pec@pectest.com",
      geographicTaxonomies: [],
      id: "c2808e95-59a7-44fb-a6df-64faf4ff3ed3",
      imported: false,
      institutionType: "PT",
      origin: "INFOCAMERE",
      taxCode: "11223345661",
      zipCode: "11223",
    },
    productId: "prod-io",
    status: "TOBEVALIDATED",
    users: [
      {
        id: "ec7ca4d5-c537-462a-8c06-e102e0c21c44",
        role: "MANAGER",
        userMailUuid: "ID_MAIL#49e16aa5-bc28-40c7-9844-6886e4647e22",
      },
    ],
    workflowType: "CONTRACT_REGISTRATION",
  },
  {
    _id: "89ad7142-24bb-48ad-8504-9c9231137i1000",
    createdAt: ISODate("2024-10-18T12:24:50.050Z"),
    institution: {
      address: "sede leg",
      city: "Milano",
      country: "IT",
      county: "MI",
      description: "Techpartner 23-02",
      digitalAddress: "pec@pectest.com",
      geographicTaxonomies: [],
      id: "c2808e95-59a7-44fb-a6df-64faf4ff3ed3",
      imported: false,
      institutionType: "PT",
      origin: "INFOCAMERE",
      taxCode: "11223345661",
      zipCode: "11223",
    },
    productId: "prod-io",
    status: "COMPLETED",
    users: [
      {
        id: "ec7ca4d5-c537-462a-8c06-e102e0c21c44",
        role: "MANAGER",
        userMailUuid: "ID_MAIL#49e16aa5-bc28-40c7-9844-6886e4647e22",
      },
    ],
    workflowType: "CONTRACT_REGISTRATION",
  },
]);

db.tokens.insertOne({
  createdAt: new Date(),
});

db.tokens.insertMany([
  {
    _id: "89ad7142-24bb-48ad-8504-9c9231137i103",
    onboardingId: "89ad7142-24bb-48ad-8504-9c9231137i103",
    type: "INSTITUTION",
    productId: "prod-io",
    contractTemplate:
      "contracts/template/io/2.4.2/io-accordo_di_adesione-v.2.4.2.html",
    createdAt: new ISODate("2021-10-20T12:53:34.000Z"),
    updatedAt: new ISODate("2023-07-25T10:24:08.119Z"),
    contractFilename: "signed_89ad7142-24bb-48ad-8504-9c9231137i103pdf",
  },
  {
    _id: "89ad7142-24bb-48ad-8504-9c9231137i105",
    onboardingId: "89ad7142-24bb-48ad-8504-9c9231137i105",
    type: "INSTITUTION",
    productId: "prod-io",
    contractTemplate:
      "contracts/template/io/2.4.2/io-accordo_di_adesione-v.2.4.2.html",
    createdAt: new ISODate("2021-10-20T12:53:34.000Z"),
    updatedAt: new ISODate("2023-07-25T10:24:08.119Z"),
  },
  {
    _id: "89ad7142-24bb-48ad-8504-9c9231137i122",
    onboardingId: "89ad7142-24bb-48ad-8504-9c9231137i122",
    type: "INSTITUTION",
    productId: "prod-io",
    contractTemplate:
      "contracts/template/io/2.4.2/io-accordo_di_adesione-v.2.4.2.html",
    createdAt: new ISODate("2021-10-20T12:53:34.000Z"),
    updatedAt: new ISODate("2023-07-25T10:24:08.119Z"),
  },
]);

db = db.getSiblingDB("selcMsCore");

db.Institution.insertOne({
  createdAt: new Date(),
});

db = db.getSiblingDB("selcUser");

db.userInstitutions.insertOne({
  createdAt: new Date(),
});
