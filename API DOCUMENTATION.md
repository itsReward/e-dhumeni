# e-Dhumeni API Documentation

## Overview

This document provides comprehensive documentation for the e-Dhumeni API, designed to track farmer performances and make informed interventions. Use this as a reference for integrating frontend applications with the backend API.

## Base URL

```
https://api.edhumeni.com/api
```

For local development:

```
http://localhost:8080/api
```

## Authentication

The API uses JWT (JSON Web Token) for authentication.

### Login

```
POST /auth/login
```

**Request Body:**

```json
{
  "username": "string",
  "password": "string"
}
```

**Response:**

```json
{
  "token": "string",
  "type": "Bearer",
  "userId": "uuid",
  "username": "string",
  "email": "string",
  "fullName": "string",
  "roles": ["string"]
}
```

### Authentication Headers

For all authenticated endpoints, include the JWT token in the Authorization header:

```
Authorization: Bearer <token>
```

## Farmers

### Get All Farmers

```
GET /farmers
```

**Query Parameters:**
- `name` (optional): Filter by name
- `region` (optional): Filter by region name
- `province` (optional): Filter by province
- `ward` (optional): Filter by ward

**Response:**

```json
[
  {
    "id": "uuid",
    "name": "string",
    "age": 0,
    "gender": "MALE | FEMALE | OTHER",
    "contactNumber": "string",
    "region": {
      "id": "uuid",
      "name": "string",
      "province": "string",
      "district": "string"
    },
    "province": "string",
    "ward": "string",
    "naturalRegion": "string",
    "soilType": "string",
    "usesFertilizer": false,
    "fertilizerType": "string",
    "manureAvailability": false,
    "usesPloughing": false,
    "usesPfumvudza": false,
    "accessToCredit": false,
    "landOwnershipType": "OWNED | LEASED | COMMUNAL | RESETTLEMENT | OTHER",
    "keepsFarmRecords": false,
    "farmSizeHectares": 0.0,
    "previousPlantedCrop": "string",
    "previousSeasonYieldKg": 0.0,
    "averageYieldPerSeasonKg": 0.0,
    "farmingPractices": ["string"],
    "conservationPractices": ["string"],
    "complianceLevel": "LOW | MEDIUM | HIGH",
    "agronomicPractices": ["string"],
    "landPreparationType": "MANUAL | MECHANIZED | CONSERVATION_TILLAGE | ZERO_TILLAGE | OTHER",
    "soilTestingDone": false,
    "plantingDate": "2023-01-01T12:00:00Z",
    "observedOffTypes": false,
    "herbicidesUsed": "string",
    "problematicPests": ["string"],
    "aeoVisitFrequency": "WEEKLY | BIWEEKLY | MONTHLY | QUARTERLY | YEARLY | NEVER",
    "challenges": ["string"],
    "hasCropInsurance": false,
    "receivesGovtSubsidies": false,
    "usesAgroforestry": false,
    "inputCostPerSeason": 0.0,
    "mainSourceOfInputs": "string",
    "socialVulnerability": "LOW | MEDIUM | HIGH",
    "educationLevel": "NONE | PRIMARY | SECONDARY | TERTIARY",
    "householdSize": 0,
    "numberOfDependents": 0,
    "maritalStatus": "SINGLE | MARRIED | DIVORCED | WIDOWED",
    "agriculturalExtensionOfficer": {
      "id": "uuid",
      "name": "string",
      "contactNumber": "string",
      "email": "string"
    },
    "needsSupport": false,
    "supportReason": "string",
    "createdAt": "2023-01-01T12:00:00Z",
    "updatedAt": "2023-01-01T12:00:00Z",
    "lastUpdatedBy": "string"
  }
]
```

### Get Farmer by ID

```
GET /farmers/{id}
```

**Response:** Same as item in the array above

### Get Farmers by Region

```
GET /farmers/region/{regionId}
```

**Response:** Array of farmer objects

### Get Farmers by AEO

```
GET /farmers/aeo/{aeoId}
```

**Response:** Array of farmer objects

### Get Farmers Needing Support

```
GET /farmers/support
```

**Response:** Array of farmer objects

### Get Farmers with Repayment Issues

```
GET /farmers/repayment-issues
```

**Response:** Array of farmer objects

### Create Farmer

```
POST /farmers
```

**Request Body:**

```json
{
  "name": "string",
  "age": 0,
  "gender": "MALE | FEMALE | OTHER",
  "contactNumber": "string",
  "regionId": "uuid",
  "province": "string",
  "ward": "string",
  "naturalRegion": "string",
  "soilType": "string",
  "usesFertilizer": false,
  "fertilizerType": "string",
  "manureAvailability": false,
  "usesPloughing": false,
  "usesPfumvudza": false,
  "accessToCredit": false,
  "landOwnershipType": "OWNED | LEASED | COMMUNAL | RESETTLEMENT | OTHER",
  "keepsFarmRecords": false,
  "farmSizeHectares": 0.0,
  "previousPlantedCrop": "string",
  "previousSeasonYieldKg": 0.0,
  "averageYieldPerSeasonKg": 0.0,
  "farmingPractices": ["string"],
  "conservationPractices": ["string"],
  "complianceLevel": "LOW | MEDIUM | HIGH",
  "agronomicPractices": ["string"],
  "landPreparationType": "MANUAL | MECHANIZED | CONSERVATION_TILLAGE | ZERO_TILLAGE | OTHER",
  "soilTestingDone": false,
  "plantingDate": "2023-01-01T12:00:00Z",
  "observedOffTypes": false,
  "herbicidesUsed": "string",
  "problematicPests": ["string"],
  "aeoVisitFrequency": "WEEKLY | BIWEEKLY | MONTHLY | QUARTERLY | YEARLY | NEVER",
  "challenges": ["string"],
  "hasCropInsurance": false,
  "receivesGovtSubsidies": false,
  "usesAgroforestry": false,
  "inputCostPerSeason": 0.0,
  "mainSourceOfInputs": "string",
  "socialVulnerability": "LOW | MEDIUM | HIGH",
  "educationLevel": "NONE | PRIMARY | SECONDARY | TERTIARY",
  "householdSize": 0,
  "numberOfDependents": 0,
  "maritalStatus": "SINGLE | MARRIED | DIVORCED | WIDOWED",
  "aeoId": "uuid",
  "needsSupport": false,
  "supportReason": "string"
}
```

**Response:** Farmer object

### Update Farmer

```
PUT /farmers/{id}
```

**Request Body:** Same as Create Farmer

**Response:** Updated farmer object

### Update Farmer Support Status

```
PATCH /farmers/{id}/support-status
```

**Query Parameters:**
- `needs_support` (required): Boolean indicating support status
- `reason` (optional): Reason for support

**Response:** Updated farmer object

### Delete Farmer

```
DELETE /farmers/{id}
```

**Response:** 204 No Content

## Contracts

### Get All Contracts

```
GET /contracts
```

**Query Parameters:**
- `farmerId` (optional): Filter by farmer ID
- `type` (optional): Filter by contract type
- `active` (optional): Filter by active status
- `repaymentStatus` (optional): Filter by repayment status

**Response:**

```json
[
  {
    "id": "uuid",
    "farmer": {
      "id": "uuid",
      "name": "string",
      "age": 0,
      "gender": "MALE | FEMALE | OTHER",
      "contactNumber": "string",
      "region": "string",
      "province": "string",
      "ward": "string",
      "farmSizeHectares": 0.0,
      "landOwnershipType": "OWNED | LEASED | COMMUNAL | RESETTLEMENT | OTHER",
      "needsSupport": false,
      "aeoName": "string"
    },
    "contractNumber": "string",
    "startDate": "2023-01-01",
    "endDate": "2023-01-01",
    "type": "BASIC | PREMIUM | COOPERATIVE | CORPORATE | GOVERNMENT",
    "expectedDeliveryKg": 0.0,
    "pricePerKg": 0.0,
    "advancePayment": 0.0,
    "inputSupportValue": 0.0,
    "signingBonus": 0.0,
    "repaymentStatus": "NOT_STARTED | IN_PROGRESS | COMPLETED | DEFAULTED | RENEGOTIATED",
    "totalRepaidAmount": 0.0,
    "totalOwedAmount": 0.0,
    "challengesMeetingTerms": "string",
    "hasLoanComponent": false,
    "active": true,
    "deliveries": [
      {
        "id": "uuid",
        "deliveryDate": "2023-01-01T12:00:00Z",
        "quantityKg": 0.0,
        "qualityGrade": "A_PLUS | A | B | C | REJECTED",
        "totalAmountPaid": 0.0
      }
    ],
    "totalDeliveredKg": 0.0,
    "deliveryCompletionPercentage": 0.0,
    "behindSchedule": false,
    "createdAt": "2023-01-01T12:00:00Z",
    "updatedAt": "2023-01-01T12:00:00Z"
  }
]
```

### Get Contract by ID

```
GET /contracts/{id}
```

**Response:** Same as item in the array above

### Get Contracts by Farmer

```
GET /contracts/farmer/{farmerId}
```

**Response:** Array of contract objects

### Get Active Contracts by Farmer

```
GET /contracts/farmer/{farmerId}/active
```

**Response:** Array of contract objects

### Get Contracts by Region

```
GET /contracts/region/{regionId}
```

**Response:** Array of contract objects

### Get Contracts by Type

```
GET /contracts/type/{type}
```

**Response:** Array of contract objects

### Get Contracts by Repayment Status

```
GET /contracts/status/{status}
```

**Response:** Array of contract objects

### Get Expired Contracts

```
GET /contracts/expired
```

**Response:** Array of contract objects

### Get At-Risk Contracts

```
GET /contracts/at-risk
```

**Response:** Array of contract objects

### Get Contract Summary

```
GET /contracts/{id}/summary
```

**Response:**

```json
{
  "id": "uuid",
  "contractNumber": "string",
  "farmerName": "string",
  "startDate": "2023-01-01",
  "endDate": "2023-01-01",
  "type": "BASIC | PREMIUM | COOPERATIVE | CORPORATE | GOVERNMENT",
  "expectedDeliveryKg": 0.0,
  "actualDeliveryKg": 0.0,
  "completionPercentage": 0.0,
  "active": true,
  "repaymentStatus": "NOT_STARTED | IN_PROGRESS | COMPLETED | DEFAULTED | RENEGOTIATED",
  "atRisk": false,
  "remainingDays": 0,
  "deliveriesCount": 0
}
```

### Create Contract

```
POST /contracts
```

**Request Body:**

```json
{
  "farmerId": "uuid",
  "contractNumber": "string",
  "startDate": "2023-01-01",
  "endDate": "2023-01-01",
  "type": "BASIC | PREMIUM | COOPERATIVE | CORPORATE | GOVERNMENT",
  "expectedDeliveryKg": 0.0,
  "pricePerKg": 0.0,
  "advancePayment": 0.0,
  "inputSupportValue": 0.0,
  "signingBonus": 0.0,
  "repaymentStatus": "NOT_STARTED | IN_PROGRESS | COMPLETED | DEFAULTED | RENEGOTIATED",
  "totalRepaidAmount": 0.0,
  "challengesMeetingTerms": "string",
  "hasLoanComponent": false,
  "active": true
}
```

**Response:** Created contract object

### Update Contract

```
PUT /contracts/{id}
```

**Request Body:** Same as Create Contract

**Response:** Updated contract object

### Deactivate Contract

```
PATCH /contracts/{id}/deactivate
```

**Response:** Updated contract object

### Delete Contract

```
DELETE /contracts/{id}
```

**Response:** 204 No Content

### Get Contracts Behind Schedule

```
GET /contracts/behind-schedule
```

**Response:** Array of contract objects

### Get Contract Trends

```
GET /contracts/trends
```

**Query Parameters:**
- `months` (optional): Number of months to analyze (default: 12)

**Response:**

```json
[
  {
    "year": 0,
    "month": 0,
    "periodLabel": "string",
    "contractCount": 0,
    "expectedDeliveryKg": 0.0,
    "actualDeliveryKg": 0.0,
    "completionRate": 0.0
  }
]
```

### Get Contract Insights

```
GET /contracts/insights
```

**Response:**

```json
{
  "totalActiveContracts": 0,
  "contractsByPerformance": {
    "AHEAD_OF_SCHEDULE": 0,
    "ON_SCHEDULE": 0,
    "BEHIND_SCHEDULE": 0,
    "SEVERELY_BEHIND": 0
  },
  "performanceByContractType": {
    "BASIC": {
      "count": 0,
      "totalExpectedKg": 0.0,
      "totalDeliveredKg": 0.0,
      "avgCompletionRate": 0.0,
      "onTrackPercentage": 0.0
    }
  },
  "criticalContracts": [
    {
      "id": "uuid",
      "contractNumber": "string",
      "farmerName": "string",
      "farmerId": "uuid",
      "endDate": "2023-01-01",
      "daysRemaining": 0,
      "completionPercentage": 0.0,
      "region": "string"
    }
  ],
  "generatedAt": "2023-01-01"
}
```

### Get Contract Recommendations

```
GET /contracts/recommendations/{farmerId}
```

**Response:**

```json
{
  "farmerId": "uuid",
  "farmerName": "string",
  "hasRecommendation": true,
  "historicalPerformance": {
    "avgDeliveryRate": 0.0,
    "avgDeliveryKg": 0.0,
    "completedContractCount": 0,
    "performanceByType": {
      "BASIC": 0.0
    }
  },
  "recommendations": ["string"],
  "adjustments": {
    "recommendedType": "BASIC | PREMIUM | COOPERATIVE | CORPORATE | GOVERNMENT",
    "recommendedSizeKg": 0.0,
    "recommendedAdvancePaymentIncrease": true
  },
  "generatedAt": "2023-01-01"
}
```

## Deliveries

### Get All Deliveries

```
GET /deliveries
```

**Query Parameters:**
- `contractId` (optional): Filter by contract ID
- `farmerId` (optional): Filter by farmer ID

**Response:**

```json
[
  {
    "id": "uuid",
    "contractId": "uuid",
    "contractNumber": "string",
    "farmerId": "uuid",
    "farmerName": "string",
    "deliveryDate": "2023-01-01T12:00:00Z",
    "quantityKg": 0.0,
    "qualityGrade": "A_PLUS | A | B | C | REJECTED",
    "moistureContent": 0.0,
    "pricePaidPerKg": 0.0,
    "totalAmountPaid": 0.0,
    "deductionAmount": 0.0,
    "deductionReason": "string",
    "receiptNumber": "string",
    "verifiedBy": "string",
    "verifiedAt": "2023-01-01T12:00:00Z",
    "notes": "string",
    "createdAt": "2023-01-01T12:00:00Z",
    "updatedAt": "2023-01-01T12:00:00Z"
  }
]
```

### Get Delivery by ID

```
GET /deliveries/{id}
```

**Response:** Same as item in the array above

### Get Deliveries by Contract

```
GET /deliveries/contract/{contractId}
```

**Response:** Array of delivery objects

### Get Deliveries by Farmer

```
GET /deliveries/farmer/{farmerId}
```

**Response:** Array of delivery objects

### Record Delivery

```
POST /deliveries
```

**Request Body:**

```json
{
  "contractId": "uuid",
  "deliveryDate": "2023-01-01T12:00:00Z",
  "quantityKg": 0.0,
  "qualityGrade": "A_PLUS | A | B | C | REJECTED",
  "moistureContent": 0.0,
  "pricePaidPerKg": 0.0,
  "totalAmountPaid": 0.0,
  "deductionAmount": 0.0,
  "deductionReason": "string",
  "receiptNumber": "string",
  "notes": "string"
}
```

**Response:** Created delivery object

### Update Delivery

```
PUT /deliveries/{id}
```

**Request Body:** Same as Record Delivery

**Response:** Updated delivery object

### Delete Delivery

```
DELETE /deliveries/{id}
```

**Response:** 204 No Content

## Regions

### Get All Regions

```
GET /regions
```

**Query Parameters:**
- `search` (optional): Search term for region name, province, or district

**Response:**

```json
[
  {
    "id": "uuid",
    "name": "string",
    "province": "string",
    "district": "string",
    "centerLatitude": 0.0,
    "centerLongitude": 0.0,
    "naturalRegion": "string",
    "averageAnnualRainfallMm": 0.0,
    "predominantSoilType": "string",
    "farmerCount": 0,
    "createdAt": "2023-01-01T12:00:00Z",
    "updatedAt": "2023-01-01T12:00:00Z"
  }
]
```

### Get Region by ID

```
GET /regions/{id}
```

**Response:** Same as item in the array above

### Get Regions by Province

```
GET /regions/province/{province}
```

**Response:** Array of region objects

### Get Regions by District

```
GET /regions/district/{district}
```

**Response:** Array of region objects

### Find Region by Coordinates

```
GET /regions/coordinates
```

**Query Parameters:**
- `latitude` (required): Latitude coordinate
- `longitude` (required): Longitude coordinate

**Response:** Region object

### Find Regions Near Coordinates

```
GET /regions/nearby
```

**Query Parameters:**
- `latitude` (required): Latitude coordinate
- `longitude` (required): Longitude coordinate
- `distanceInMeters` (optional): Distance in meters (default: 10000)

**Response:** Array of region objects

### Get Regions for Map

```
GET /regions/map
```

**Response:**

```json
[
  {
    "id": "string",
    "name": "string",
    "province": "string",
    "district": "string",
    "centerLatitude": 0.0,
    "centerLongitude": 0.0,
    "farmerCount": 0
  }
]
```

### Get Region Statistics

```
GET /regions/{id}/stats
```

**Response:**

```json
{
  "id": "uuid",
  "name": "string",
  "province": "string",
  "district": "string",
  "farmerCount": 0,
  "farmersNeedingSupportCount": 0,
  "supportPercentage": 0.0
}
```

### Create Region

```
POST /regions
```

**Request Body:**

```json
{
  "name": "string",
  "province": "string",
  "district": "string",
  "wktBoundary": "string",
  "centerLatitude": 0.0,
  "centerLongitude": 0.0,
  "naturalRegion": "string",
  "averageAnnualRainfallMm": 0.0,
  "predominantSoilType": "string"
}
```

**Response:** Created region object

### Update Region

```
PUT /regions/{id}
```

**Request Body:** Same as Create Region

**Response:** Updated region object

### Update Region Boundary

```
PATCH /regions/{id}/boundary
```

**Query Parameters:**
- `wkt` (required): Well-Known Text representation of the boundary

**Response:** Updated region object

### Delete Region

```
DELETE /regions/{id}
```

**Response:** 204 No Content

## Agricultural Extension Officers (AEOs)

### Get All AEOs

```
GET /aeos
```

**Query Parameters:**
- `search` (optional): Search term for AEO name, email, or employee ID

**Response:**

```json
[
  {
    "id": "uuid",
    "name": "string",
    "contactNumber": "string",
    "email": "string",
    "employeeId": "string",
    "qualification": "string",
    "yearsOfExperience": 0,
    "assignedRegions": [
      {
        "id": "uuid",
        "name": "string",
        "province": "string",
        "district": "string"
      }
    ],
    "farmerCount": 0,
    "farmersNeedingSupportCount": 0,
    "createdAt": "2023-01-01T12:00:00Z",
    "updatedAt": "2023-01-01T12:00:00Z"
  }
]
```

### Get AEO by ID

```
GET /aeos/{id}
```

**Response:** Same as item in the array above

### Get AEOs by Region

```
GET /aeos/region/{regionId}
```

**Response:** Array of AEO objects

### Get AEOs with Farmers Needing Support

```
GET /aeos/support-needed
```

**Response:** Array of AEO objects

### Create AEO

```
POST /aeos
```

**Request Body:**

```json
{
  "name": "string",
  "contactNumber": "string",
  "email": "string",
  "employeeId": "string",
  "qualification": "string",
  "yearsOfExperience": 0,
  "assignedRegionIds": ["uuid"]
}
```

**Response:** Created AEO object

### Update AEO

```
PUT /aeos/{id}
```

**Request Body:** Same as Create AEO

**Response:** Updated AEO object

### Assign Region to AEO

```
POST /aeos/{id}/regions/{regionId}
```

**Response:** Updated AEO object

### Unassign Region from AEO

```
DELETE /aeos/{id}/regions/{regionId}
```

**Response:** Updated AEO object

### Delete AEO

```
DELETE /aeos/{id}
```

**Response:** 204 No Content

### Get AEO Statistics

```
GET /aeos/{id}/stats
```

**Response:**

```json
{
  "id": "uuid",
  "name": "string",
  "totalFarmers": 0,
  "farmersNeedingSupport": 0,
  "supportPercentage": 0.0,
  "assignedRegions": 0
}
```

## Map Integration

### Get All Regions for Map

```
GET /map/regions
```

**Response:**

```json
[
  {
    "id": "uuid",
    "name": "string",
    "province": "string",
    "district": "string",
    "centerLatitude": 0.0,
    "centerLongitude": 0.0,
    "farmerCount": 0,
    "farmersNeedingSupportCount": 0
  }
]
```

### Get Farmers in Region

```
GET /map/regions/{regionId}/farmers
```

**Response:** Array of farmer objects

### Find Region by Coordinates

```
GET /map/coordinates
```

**Query Parameters:**
- `latitude` (required): Latitude coordinate
- `longitude` (required): Longitude coordinate

**Response:** Region map object

### Get Farmers Needing Support in Region

```
GET /map/regions/{regionId}/support-alert
```

**Response:** Array of farmer objects

### Get All Regions with Support Alerts

```
GET /map/support-alerts
```

**Response:** Array of region map objects

## Support Alerts

### Get All Farmers Needing Support

```
GET /alerts/farmers
```

**Response:** Array of farmer objects

### Get Farmers Needing Support by Region

```
GET /alerts/farmers/region/{regionId}
```

**Response:** Array of farmer objects

### Get Farmers with Repayment Issues

```
GET /alerts/farmers/repayment-issues
```

**Response:** Array of farmer objects

### Get High Priority Farmers

```
GET /alerts/farmers/high-priority
```

**Response:** Array of farmer objects

### Get At-Risk Contracts

```
GET /alerts/contracts/at-risk
```

**Response:**

```json
[
  {
    "id": "uuid",
    "contractNumber": "string",
    "farmerName": "string",
    "farmerId": "uuid",
    "endDate": "2023-01-01",
    "expectedDeliveryKg": 0.0,
    "actualDeliveryKg": 0.0,
    "completionPercentage": 0.0,
    "daysRemaining": 0
  }
]
```

### Get Alert Summary

```
GET /alerts/summary
```

**Response:**

```json
{
  "totalFarmersNeedingSupport": 0,
  "percentageNeedingSupport": 0.0,
  "totalAtRiskContracts": 0,
  "supportByReason": {
    "string": 0
  },
  "supportByRegion": {
    "string": 0
  },
  "supportByVulnerability": {
    "LOW": 0,
    "MEDIUM": 0,
    "HIGH": 0
  },
  "supportByEducation": {
    "NONE": 0,
    "PRIMARY": 0,
    "SECONDARY": 0,
    "TERTIARY": 0
  },
  "mostCommonReason": "string",
  "lastUpdated": "2023-01-01T12:00:00Z"
}
```

### Run Support Assessment

```
POST /alerts/assess
```

**Response:**

```json
{
  "status": "string",
  "message": "string",
  "summary": {
    "totalFarmersNeedingSupport": 0,
    "percentageNeedingSupport": 0.0,
    "totalAtRiskContracts": 0,
    "supportByReason": {
      "string": 0
    }
  }
}
```

### Mark Farmer as Needing Support

```
POST /alerts/farmer/{id}/mark
```

**Query Parameters:**
- `reason` (required): Reason for support

**Response:** Updated farmer object

### Resolve Farmer Support

```
POST /alerts/farmer/{id}/resolve
```

**Query Parameters:**
- `resolutionNotes` (required): Notes about how the issue was resolved

**Response:** Updated farmer object

### Assess Individual Farmer

```
POST /alerts/assess/{id}
```

**Response:**

```json
{
  "farmerId": "uuid",
  "farmerName": "string",
  "needsSupport": true,
  "supportReason": "string",
  "assessedBy": "string",
  "assessedAt": "2023-01-01T12:00:00Z"
}
```

## Farmer Assessments

### Calculate Farmer Risk Score

```
GET /assessments/farmer/{farmerId}/risk-score
```

**Response:**

```json
{
  "farmerId": "uuid",
  "farmerName": "string",
  "riskScore": 0.0,
  "riskLevel": "VERY_LOW | LOW | MEDIUM | HIGH | VERY_HIGH",
  "riskFactors": {
    "deliveryPerformance": {
      "score": 0,
      "percentage": 0.0,
      "maxScore": 0
    },
    "yieldTrend": {
      "score": 0,
      "ratio": 0.0,
      "maxScore": 0
    },
    "socialVulnerability": {
      "score": 0,
      "level": "LOW | MEDIUM | HIGH",
      "maxScore": 0
    },
    "agriculturalPractices": {
      "score": 0,
      "maxScore": 0
    },
    "supportNetwork": {
      "score": 0,
      "visitFrequency": "WEEKLY | BIWEEKLY | MONTHLY | QUARTERLY | YEARLY | NEVER",
      "maxScore": 0
    },
    "financialRisk": {
      "score": 0,
      "maxScore": 0
    }
  },
  "recommendedActions": ["string"],
  "assessedAt": "2023-01-01"
}
```

### Analyze Region Farmers

```
GET /assessments/region/{regionId}/analysis
```

**Response:**

```json
{
  "regionId": "uuid",
  "totalFarmers": 0,
  "highRiskFarmers": 0,
  "highRiskPercentage": 0.0,
  "farmingPracticesDistribution": {
    "string": 0
  },
  "conservationPracticesDistribution": {
    "string": 0
  },
  "challengesDistribution": {
    "string": 0
  },
  "supportReasons": {
    "string": 0
  },
  "averageFarmSize": 0.0,
  "averageYield": 0.0,
  "recommendedInterventions": ["string"],
  "analyzedAt": "2023-01-01"
}
```

## Reports

### Generate Farmer Performance Report

```
GET /reports/farmer/{farmerId}
```

**Response:**

```json
{
  "farmerId": "uuid",
  "farmerName": "string",
  "region": "string",
  "totalContracts": 0,
  "activeContracts": 0,
  "totalContractedAmount": 0.0,
  "totalDeliveredAmount": 0.0,
  "deliveryPercentage": 0.0,
  "qualityDistribution": {
    "A_PLUS": 0.0,
    "A": 0.0,
    "B": 0.0,
    "C": 0.0,
    "REJECTED": 0.0
  },
  "contractDetails": [
    {
      "contractNumber": "string",
      "expectedDeliveryKg": 0.0,
      "actualDeliveryKg": 0.0,
      "completionPercentage": 0.0,
      "active": true,
      "behindSchedule": false
    }
  ],
  "generatedAt": "2023-01-01T12:00:00Z"
}
```

### Generate Region Performance Report

```
GET /reports/region/{regionId}
```

**Response:**

```json
{
  "regionId": "uuid",
  "regionName": "string",
  "province": "string",
  "district": "string",
  "totalFarmers": 0,
  "farmersNeedingSupport": 0,
  "supportPercentage": 0.0,
  "totalContracts": 0,
  "activeContracts": 0,
  "completedContracts": 0,
  "atRiskContracts": 0,
  "totalContractedAmount": 0.0,
  "totalDeliveredAmount": 0.0,
  "deliveryPercentage": 0.0,
  "topPerformingFarmers": [
    {
      "farmerId": "uuid",
      "farmerName": "string",
      "contractedAmount": 0.0,
      "deliveredAmount": 0.0,
      "deliveryPercentage": 0.0
    }
  ],
  "farmersNeedingSupport": [
    {
      "farmerId": "uuid",
      "farmerName": "string",
      "supportReason": "string"
    }
  ],
  "generatedAt": "2023-01-01T12:00:00Z"
}
```

### Generate System Performance Report

```
GET /reports/system
```

**Response:**

```json
{
  "totalFarmers": 0,
  "totalContracts": 0,
  "totalDeliveries": 0,
  "totalRegions": 0,
  "farmersNeedingSupport": 0,
  "supportPercentage": 0.0,
  "activeContracts": 0,
  "completedContracts": 0,
  "atRiskContracts": 0,
  "totalContractedAmount": 0.0,
  "totalDeliveredAmount": 0.0,
  "deliveryPercentage": 0.0,
  "regionStatistics": [
    {
      "regionId": "uuid",
      "regionName": "string",
      "province": "string",
      "district": "string",
      "farmerCount": 0,
      "supportCount": 0
    }
  ],
  "generatedAt": "2023-01-01T12:00:00Z"
}
```

## Dashboard

### Get Dashboard Summary

```
GET /dashboard/summary
```

**Response:**

```json
{
  "totalFarmers": 0,
  "totalContracts": 0,
  "activeContracts": 0,
  "farmersNeedingSupport": 0,
  "supportPercentage": 0.0,
  "atRiskContracts": 0,
  "totalRegions": 0,
  "totalDeliveredKg": 0.0,
  "topRegionsByFarmers": [
    {
      "regionName": "string",
      "farmerCount": 0
    }
  ],
  "deliveryTrend": [
    {
      "date": "2023-01-01",
      "totalQuantity": 0.0
    }
  ],
  "lastUpdated": "2023-01-01T12:00:00Z"
}
```

### Get Region Statistics

```
GET /dashboard/regions/stats
```

**Response:**

```json
[
  {
    "regionId": "uuid",
    "regionName": "string",
    "province": "string",
    "district": "string",
    "farmerCount": 0,
    "supportCount": 0,
    "supportPercentage": 0.0
  }
]
```

### Get Contract Completion Statistics

```
GET /dashboard/contracts/completion
```

**Response:**

```json
{
  "totalContracts": 0,
  "completedContracts": 0,
  "completionPercentage": 0.0,
  "partiallyCompletedContracts": 0,
  "notStartedContracts": 0,
  "completionByType": {
    "BASIC": {
      "completed": 0,
      "total": 0,
      "percentage": 0.0
    }
  }
}
```

### Get Delivery Quality Statistics

```
GET /dashboard/deliveries/quality
```

**Response:**

```json
{
  "totalDeliveries": 0,
  "deliveriesByQuality": {
    "A_PLUS": 0,
    "A": 0,
    "B": 0,
    "C": 0,
    "REJECTED": 0
  },
  "qualityDistribution": {
    "A_PLUS": 0.0,
    "A": 0.0,
    "B": 0.0,
    "C": 0.0,
    "REJECTED": 0.0
  },
  "averageDeliverySize": 0.0
}
```

### Get Contract Time Statistics

```
GET /dashboard/contracts/time
```

**Response:**

```json
{
  "averageContractDuration": 0,
  "activeContracts": 0,
  "expiredContracts": 0,
  "expiringSoonContracts": 0,
  "contractsByType": {
    "BASIC": 0,
    "PREMIUM": 0,
    "COOPERATIVE": 0,
    "CORPORATE": 0,
    "GOVERNMENT": 0
  }
}
```

## Users

### Get All Users

```
GET /users
```

**Response:**

```json
[
  {
    "id": "uuid",
    "username": "string",
    "email": "string",
    "fullName": "string",
    "role": "ADMIN | MANAGER | AEO | USER",
    "enabled": true,
    "createdAt": "2023-01-01T12:00:00Z",
    "updatedAt": "2023-01-01T12:00:00Z"
  }
]
```

### Get User by ID

```
GET /users/{id}
```

**Response:** Same as item in the array above

### Create User

```
POST /users
```

**Request Body:**

```json
{
  "username": "string",
  "password": "string",
  "email": "string",
  "fullName": "string",
  "role": "ADMIN | MANAGER | AEO | USER",
  "enabled": true
}
```

**Response:** Created user object

### Update User

```
PUT /users/{id}
```

**Request Body:** Same as Create User

**Response:** Updated user object

### Change Password

```
POST /users/{id}/change-password
```

**Request Body:**

```json
{
  "currentPassword": "string",
  "newPassword": "string"
}
```

**Response:** Updated user object

### Reset Password (Admin only)

```
POST /users/{id}/reset-password
```

**Query Parameters:**
- `newPassword` (required): New password for the user

**Response:** Updated user object

### Delete User

```
DELETE /users/{id}
```

**Response:** 204 No Content

### Toggle User Status

```
POST /users/{id}/toggle-status
```

**Response:** Updated user object

### Get User Statistics

```
GET /users/stats
```

**Response:**

```json
{
  "totalUsers": 0,
  "activeUsers": 0,
  "inactiveUsers": 0,
  "usersByRole": {
    "ADMIN": 0,
    "MANAGER": 0,
    "AEO": 0,
    "USER": 0
  }
}
```

## Offline Data Synchronization

### Download Offline Data

```
GET /sync/download
```

**Query Parameters:**
- `regionIds` (optional): List of region IDs to include in the download

**Response:**

```json
{
  "farmers": [
    {
      "id": "uuid",
      "name": "string",
      "age": 0,
      "gender": "MALE | FEMALE | OTHER",
      "contactNumber": "string",
      "regionId": "uuid",
      "province": "string",
      "ward": "string",
      "naturalRegion": "string",
      "soilType": "string",
      "usesFertilizer": false,
      "fertilizerType": "string",
      "manureAvailability": false,
      "usesPloughing": false,
      "usesPfumvudza": false,
      "accessToCredit": false,
      "landOwnershipType": "OWNED | LEASED | COMMUNAL | RESETTLEMENT | OTHER",
      "keepsFarmRecords": false,
      "farmSizeHectares": 0.0,
      "previousPlantedCrop": "string",
      "previousSeasonYieldKg": 0.0,
      "averageYieldPerSeasonKg": 0.0,
      "farmingPractices": ["string"],
      "conservationPractices": ["string"],
      "complianceLevel": "LOW | MEDIUM | HIGH",
      "agronomicPractices": ["string"],
      "landPreparationType": "MANUAL | MECHANIZED | CONSERVATION_TILLAGE | ZERO_TILLAGE | OTHER",
      "soilTestingDone": false,
      "plantingDate": "2023-01-01T12:00:00Z",
      "observedOffTypes": false,
      "herbicidesUsed": "string",
      "problematicPests": ["string"],
      "aeoVisitFrequency": "WEEKLY | BIWEEKLY | MONTHLY | QUARTERLY | YEARLY | NEVER",
      "challenges": ["string"],
      "hasCropInsurance": false,
      "receivesGovtSubsidies": false,
      "usesAgroforestry": false,
      "inputCostPerSeason": 0.0,
      "mainSourceOfInputs": "string",
      "socialVulnerability": "LOW | MEDIUM | HIGH",
      "educationLevel": "NONE | PRIMARY | SECONDARY | TERTIARY",
      "householdSize": 0,
      "numberOfDependents": 0,
      "maritalStatus": "SINGLE | MARRIED | DIVORCED | WIDOWED",
      "needsSupport": false,
      "supportReason": "string",
      "updatedAt": "2023-01-01T12:00:00Z"
    }
  ],
  "regions": [
    {
      "id": "uuid",
      "name": "string",
      "province": "string",
      "district": "string",
      "centerLatitude": 0.0,
      "centerLongitude": 0.0,
      "naturalRegion": "string"
    }
  ],
  "timestamp": "2023-01-01T12:00:00Z",
  "expiresAt": "2023-01-08T12:00:00Z"
}
```

### Upload Offline Changes

```
POST /sync/upload
```

**Request Body:**

```json
[
  {
    "id": "uuid",
    "name": "string",
    "age": 0,
    "gender": "MALE | FEMALE | OTHER",
    "contactNumber": "string",
    "province": "string",
    "ward": "string",
    "usesFertilizer": false,
    "fertilizerType": "string",
    "soilTestingDone": false,
    "needsSupport": false,
    "supportReason": "string",
    "lastModified": "2023-01-01T12:00:00Z"
  }
]
```

**Response:**

```json
{
  "success": 0,
  "failed": 0,
  "successfulSyncs": ["uuid"],
  "failedSyncs": [
    {
      "id": "uuid",
      "reason": "string"
    }
  ],
  "timestamp": "2023-01-01T12:00:00Z"
}
```

## Data Entities

### Contract Types

- `BASIC`: Standard contract with basic terms
- `PREMIUM`: Enhanced contract with better terms for high-performing farmers
- `COOPERATIVE`: Contract through a cooperative organization
- `CORPORATE`: Contract with a corporate partner
- `GOVERNMENT`: Contract supported by government programs

### Repayment Status

- `NOT_STARTED`: Farmer has not begun fulfilling contract terms
- `IN_PROGRESS`: Farmer has made partial deliveries
- `COMPLETED`: Farmer has completed all contract deliveries
- `DEFAULTED`: Farmer has failed to meet contract terms
- `RENEGOTIATED`: Contract terms have been renegotiated

### Quality Grades

- `A_PLUS`: Premium quality
- `A`: Excellent quality
- `B`: Good quality
- `C`: Acceptable quality
- `REJECTED`: Below acceptable standards

### Gender

- `MALE`
- `FEMALE`
- `OTHER`

### Land Ownership Types

- `OWNED`: Farmer owns the land
- `LEASED`: Farmer leases the land
- `COMMUNAL`: Communal land
- `RESETTLEMENT`: Government resettlement program
- `OTHER`: Other arrangements

### Compliance Levels

- `LOW`: Poor compliance with standards
- `MEDIUM`: Average compliance with standards
- `HIGH`: Strong compliance with standards

### Land Preparation Types

- `MANUAL`: Manual preparation methods
- `MECHANIZED`: Machine-based preparation methods
- `CONSERVATION_TILLAGE`: Conservation tillage methods
- `ZERO_TILLAGE`: No-till methods
- `OTHER`: Other preparation methods

### Visit Frequency

- `WEEKLY`: Once per week
- `BIWEEKLY`: Every two weeks
- `MONTHLY`: Once per month
- `QUARTERLY`: Every three months
- `YEARLY`: Once per year
- `NEVER`: No regular visits

### Vulnerability Levels

- `LOW`: Low social vulnerability
- `MEDIUM`: Medium social vulnerability
- `HIGH`: High social vulnerability

### Education Levels

- `NONE`: No formal education
- `PRIMARY`: Primary education
- `SECONDARY`: Secondary education
- `TERTIARY`: Tertiary education

### Marital Status

- `SINGLE`
- `MARRIED`
- `DIVORCED`
- `WIDOWED`

### User Roles

- `ADMIN`: Full system access
- `MANAGER`: Regional management access
- `AEO`: Agricultural extension officer access
- `USER`: Basic system access

## Error Responses

The API uses standard HTTP status codes to indicate the success or failure of requests:

- `200 OK`: Request successful
- `201 Created`: Resource created successfully
- `204 No Content`: Request successful, no content to return
- `400 Bad Request`: Invalid request parameters
- `401 Unauthorized`: Authentication required
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Request conflicts with current state
- `500 Internal Server Error`: Server-side error

Error responses include a JSON body with details:

```json
{
  "timestamp": "2023-01-01T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid input data",
  "path": "/api/farmers"
}
```

## Best Practices

1. **Authentication**: Always include the JWT token in requests to authenticated endpoints
2. **Error Handling**: Implement robust error handling for API responses
3. **Pagination**: For endpoints that return large collections, consider implementing pagination
4. **Caching**: Cache frequently accessed data to improve performance
5. **Offline Support**: Implement offline data synchronization when connectivity is limited

## Technical Support

For technical support or questions about the API, contact:

- Email: api-support@edhumeni.com
- Documentation: https://docs.edhumeni.com