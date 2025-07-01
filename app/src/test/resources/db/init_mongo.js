db = db.getSiblingDB("selcOnboarding");

db.onboardings.insertOne({
  createdAt: new Date(),
});

db.onboardings.insertOne({
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
    zipCode: "11223"
  },
  productId: "prod-io",
  status: "PENDING",
  users: [
    {
      id: "ec7ca4d5-c537-462a-8c06-e102e0c21c44",
      role: "MANAGER",
      userMailUuid: "ID_MAIL#49e16aa5-bc28-40c7-9844-6886e4647e22"
    }
  ],
  workflowType: "USERS_PG"
});


db.tokens.insertOne({
  createdAt: new Date(),
});

db.tokens.insertOne({
  _id: "89ad7142-24bb-48ad-8504-9c9231137i103",
  onboardingId: "89ad7142-24bb-48ad-8504-9c9231137i103",
  type: "INSTITUTION",
  productId: "prod-io",
  contractTemplate: "contracts/template/io/2.4.2/io-accordo_di_adesione-v.2.4.2.html",
  createdAt: new ISODate("2021-10-20T12:53:34.000Z"),
  updatedAt: new ISODate("2023-07-25T10:24:08.119Z")
});

db = db.getSiblingDB("selcMsCore");

db.Institution.insertOne({
  createdAt: new Date(),
});

db = db.getSiblingDB("selcUser");

db.userInstitutions.insertOne({
  createdAt: new Date(),
});
