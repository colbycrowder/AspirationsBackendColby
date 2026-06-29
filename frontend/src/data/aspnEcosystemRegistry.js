import civicAdministrationIcon from "../assets/credentials/CIVIC ADMINISTRATION.png";
import civicResearchIcon from "../assets/credentials/CIVIC RESEARCH.png";
import communityEngagementIcon from "../assets/credentials/COMMUNITY ENGAGEMENT.png";
import dataEvaluationBasicsIcon from "../assets/credentials/DATA & EVALUATION BASICS.png";
import digitalCivicOperationsIcon from "../assets/credentials/DIGITAL CIVIC OPERATIONS.png";
import grantsResourceDevelopmentIcon from "../assets/credentials/GRANTS & RESOURCE DEVELOPMENT.png";
import projectImplementationIcon from "../assets/credentials/PROJECT IMPLEMENTATION.png";
import publicCommunicationIcon from "../assets/credentials/PUBLIC COMMUNICATION.png";
import publicSectorCareerReadinessIcon from "../assets/credentials/PUBLIC-SECTOR CAREER READINESS.png";
import regulatoryLiteracyIcon from "../assets/credentials/REGULATORY LITERACY.png";

export const ASPN_ECOSYSTEM_REGISTRY_VERSION = "1.0";

export const ASPN_PATHWAY_IDS = Object.freeze({
  CIVIC_RESEARCH: "civic-research",
  DATA_EVALUATION: "data-evaluation",
  PUBLIC_COMMUNICATION: "public-communication",
  COMMUNITY_ENGAGEMENT: "community-engagement",
  PROJECT_IMPLEMENTATION: "project-implementation",
  CIVIC_ADMINISTRATION: "civic-administration",
});

export const ASPN_CREDENTIAL_IDS = Object.freeze({
  CIVIC_RESEARCH: "civic-research",
  DATA_EVALUATION_BASICS: "data-evaluation-basics",
  PUBLIC_COMMUNICATION: "public-communication",
  COMMUNITY_ENGAGEMENT: "community-engagement",
  PROJECT_IMPLEMENTATION: "project-implementation",
  CIVIC_ADMINISTRATION: "civic-administration",
  REGULATORY_LITERACY: "regulatory-literacy",
  DIGITAL_CIVIC_OPERATIONS: "digital-civic-operations",
  GRANTS_RESOURCE_DEVELOPMENT: "grants-resource-development",
  PUBLIC_SECTOR_CAREER_READINESS: "public-sector-career-readiness",
});

const coreCredentials = [
  {
    id: ASPN_CREDENTIAL_IDS.CIVIC_RESEARCH,
    name: "Civic Research",
    icon: civicResearchIcon,
    iconAlt: "Civic Research credential icon",
    description: "Gather evidence, investigate civic questions, and communicate research that helps communities understand issues.",
    type: "core",
    pathwayIds: [ASPN_PATHWAY_IDS.CIVIC_RESEARCH],
  },
  {
    id: ASPN_CREDENTIAL_IDS.DATA_EVALUATION_BASICS,
    name: "Data & Evaluation Basics",
    icon: dataEvaluationBasicsIcon,
    iconAlt: "Data & Evaluation Basics credential icon",
    description: "Build foundational skills for organizing data, measuring participation, and understanding program results.",
    type: "core",
    pathwayIds: [ASPN_PATHWAY_IDS.DATA_EVALUATION],
  },
  {
    id: ASPN_CREDENTIAL_IDS.PUBLIC_COMMUNICATION,
    name: "Public Communication",
    icon: publicCommunicationIcon,
    iconAlt: "Public Communication credential icon",
    description: "Share civic information clearly through writing, speaking, outreach, and digital communication.",
    type: "core",
    pathwayIds: [ASPN_PATHWAY_IDS.PUBLIC_COMMUNICATION],
  },
  {
    id: ASPN_CREDENTIAL_IDS.COMMUNITY_ENGAGEMENT,
    name: "Community Engagement",
    icon: communityEngagementIcon,
    iconAlt: "Community Engagement credential icon",
    description: "Support community participation, relationship building, outreach, and collaborative civic action.",
    type: "core",
    pathwayIds: [ASPN_PATHWAY_IDS.COMMUNITY_ENGAGEMENT],
  },
  {
    id: ASPN_CREDENTIAL_IDS.PROJECT_IMPLEMENTATION,
    name: "Project Implementation",
    icon: projectImplementationIcon,
    iconAlt: "Project Implementation credential icon",
    description: "Plan and carry out civic projects by coordinating tasks, people, resources, and results.",
    type: "core",
    pathwayIds: [ASPN_PATHWAY_IDS.PROJECT_IMPLEMENTATION],
  },
  {
    id: ASPN_CREDENTIAL_IDS.CIVIC_ADMINISTRATION,
    name: "Civic Administration",
    icon: civicAdministrationIcon,
    iconAlt: "Civic Administration credential icon",
    description: "Understand and support the operational systems that help public and community organizations serve people.",
    type: "core",
    pathwayIds: [ASPN_PATHWAY_IDS.CIVIC_ADMINISTRATION],
  },
];

const advancedCredentials = [
  {
    id: ASPN_CREDENTIAL_IDS.REGULATORY_LITERACY,
    name: "Regulatory Literacy",
    icon: regulatoryLiteracyIcon,
    iconAlt: "Regulatory Literacy credential icon",
    description: "Explore how policies, rules, and public requirements shape civic programs and community decisions.",
    type: "advanced",
    pathwayIds: [ASPN_PATHWAY_IDS.CIVIC_ADMINISTRATION, ASPN_PATHWAY_IDS.CIVIC_RESEARCH],
  },
  {
    id: ASPN_CREDENTIAL_IDS.DIGITAL_CIVIC_OPERATIONS,
    name: "Digital Civic Operations",
    icon: digitalCivicOperationsIcon,
    iconAlt: "Digital Civic Operations credential icon",
    description: "Use digital tools and data practices to support transparent, organized, and effective civic operations.",
    type: "advanced",
    pathwayIds: [ASPN_PATHWAY_IDS.DATA_EVALUATION, ASPN_PATHWAY_IDS.CIVIC_ADMINISTRATION],
  },
  {
    id: ASPN_CREDENTIAL_IDS.GRANTS_RESOURCE_DEVELOPMENT,
    name: "Grants & Resource Development",
    icon: grantsResourceDevelopmentIcon,
    iconAlt: "Grants & Resource Development credential icon",
    description: "Understand how civic projects identify resources, communicate needs, and build support for implementation.",
    type: "advanced",
    pathwayIds: [ASPN_PATHWAY_IDS.PROJECT_IMPLEMENTATION, ASPN_PATHWAY_IDS.COMMUNITY_ENGAGEMENT],
  },
  {
    id: ASPN_CREDENTIAL_IDS.PUBLIC_SECTOR_CAREER_READINESS,
    name: "Public-Sector Career Readiness",
    icon: publicSectorCareerReadinessIcon,
    iconAlt: "Public-Sector Career Readiness credential icon",
    description: "Connect civic skills, workplace habits, and public-service knowledge to future public-sector pathways.",
    type: "advanced",
    pathwayIds: Object.values(ASPN_PATHWAY_IDS),
  },
];

const pathways = [
  {
    id: ASPN_PATHWAY_IDS.CIVIC_RESEARCH,
    name: "Civic Research",
    description: "Investigate community questions, gather evidence, and turn findings into useful civic knowledge.",
    relatedCoreCredentialId: ASPN_CREDENTIAL_IDS.CIVIC_RESEARCH,
    relatedAdvancedCredentialIds: [
      ASPN_CREDENTIAL_IDS.REGULATORY_LITERACY,
      ASPN_CREDENTIAL_IDS.PUBLIC_SECTOR_CAREER_READINESS,
    ],
    exampleCivicRoleFamilies: [
      "Research Assistant",
      "Policy Research Assistant",
      "Community Research Coordinator",
      "Program Evaluator",
    ],
  },
  {
    id: ASPN_PATHWAY_IDS.DATA_EVALUATION,
    name: "Data & Evaluation",
    description: "Use data to understand participation, measure results, and improve programs and public initiatives.",
    relatedCoreCredentialId: ASPN_CREDENTIAL_IDS.DATA_EVALUATION_BASICS,
    relatedAdvancedCredentialIds: [
      ASPN_CREDENTIAL_IDS.DIGITAL_CIVIC_OPERATIONS,
      ASPN_CREDENTIAL_IDS.PUBLIC_SECTOR_CAREER_READINESS,
    ],
    exampleCivicRoleFamilies: [
      "Data Analyst",
      "Performance Analyst",
      "Research Statistician",
      "Evaluation Assistant",
    ],
  },
  {
    id: ASPN_PATHWAY_IDS.PUBLIC_COMMUNICATION,
    name: "Public Communication",
    description: "Share public information clearly, tell community stories, and connect people with civic opportunities.",
    relatedCoreCredentialId: ASPN_CREDENTIAL_IDS.PUBLIC_COMMUNICATION,
    relatedAdvancedCredentialIds: [ASPN_CREDENTIAL_IDS.PUBLIC_SECTOR_CAREER_READINESS],
    exampleCivicRoleFamilies: [
      "Communications Coordinator",
      "Public Information Assistant",
      "Outreach Specialist",
      "Digital Communications Assistant",
    ],
  },
  {
    id: ASPN_PATHWAY_IDS.COMMUNITY_ENGAGEMENT,
    name: "Community Engagement",
    description: "Build relationships, support participation, and help communities organize around shared priorities.",
    relatedCoreCredentialId: ASPN_CREDENTIAL_IDS.COMMUNITY_ENGAGEMENT,
    relatedAdvancedCredentialIds: [
      ASPN_CREDENTIAL_IDS.GRANTS_RESOURCE_DEVELOPMENT,
      ASPN_CREDENTIAL_IDS.PUBLIC_SECTOR_CAREER_READINESS,
    ],
    exampleCivicRoleFamilies: [
      "Community Engagement Specialist",
      "Volunteer Coordinator",
      "Community Outreach Coordinator",
      "Youth Program Specialist",
    ],
  },
  {
    id: ASPN_PATHWAY_IDS.PROJECT_IMPLEMENTATION,
    name: "Project Implementation",
    description: "Plan and carry out civic projects by coordinating people, resources, schedules, and results.",
    relatedCoreCredentialId: ASPN_CREDENTIAL_IDS.PROJECT_IMPLEMENTATION,
    relatedAdvancedCredentialIds: [
      ASPN_CREDENTIAL_IDS.GRANTS_RESOURCE_DEVELOPMENT,
      ASPN_CREDENTIAL_IDS.PUBLIC_SECTOR_CAREER_READINESS,
    ],
    exampleCivicRoleFamilies: [
      "Program Coordinator",
      "Project Coordinator",
      "Community Program Specialist",
      "Operations Assistant",
    ],
  },
  {
    id: ASPN_PATHWAY_IDS.CIVIC_ADMINISTRATION,
    name: "Civic Administration",
    description: "Support the systems, processes, and public operations that help civic organizations serve communities.",
    relatedCoreCredentialId: ASPN_CREDENTIAL_IDS.CIVIC_ADMINISTRATION,
    relatedAdvancedCredentialIds: [
      ASPN_CREDENTIAL_IDS.REGULATORY_LITERACY,
      ASPN_CREDENTIAL_IDS.DIGITAL_CIVIC_OPERATIONS,
      ASPN_CREDENTIAL_IDS.PUBLIC_SECTOR_CAREER_READINESS,
    ],
    exampleCivicRoleFamilies: [
      "Administrative Analyst",
      "Program Administrator",
      "Public Service Specialist",
      "Government Services Assistant",
    ],
  },
];

const programs = [
  {
    id: "youth2lead",
    name: "Youth2Lead",
    credentialIds: [
      ASPN_CREDENTIAL_IDS.COMMUNITY_ENGAGEMENT,
      ASPN_CREDENTIAL_IDS.PUBLIC_COMMUNICATION,
      ASPN_CREDENTIAL_IDS.PROJECT_IMPLEMENTATION,
      ASPN_CREDENTIAL_IDS.CIVIC_RESEARCH,
    ],
  },
  {
    id: "global-civic-movements",
    name: "Global Civic Movements",
    credentialIds: [
      ASPN_CREDENTIAL_IDS.CIVIC_RESEARCH,
      ASPN_CREDENTIAL_IDS.PUBLIC_COMMUNICATION,
      ASPN_CREDENTIAL_IDS.CIVIC_ADMINISTRATION,
    ],
  },
  {
    id: "youth-advisory-boards",
    name: "Youth Advisory Boards",
    credentialIds: [
      ASPN_CREDENTIAL_IDS.CIVIC_ADMINISTRATION,
      ASPN_CREDENTIAL_IDS.COMMUNITY_ENGAGEMENT,
      ASPN_CREDENTIAL_IDS.PUBLIC_COMMUNICATION,
      ASPN_CREDENTIAL_IDS.PROJECT_IMPLEMENTATION,
    ],
  },
  {
    id: "civic-fellows",
    name: "Civic Fellows",
    credentialIds: [
      ASPN_CREDENTIAL_IDS.CIVIC_RESEARCH,
      ASPN_CREDENTIAL_IDS.DATA_EVALUATION_BASICS,
      ASPN_CREDENTIAL_IDS.PROJECT_IMPLEMENTATION,
      ASPN_CREDENTIAL_IDS.PUBLIC_COMMUNICATION,
    ],
  },
  {
    id: "community-service-activities",
    name: "Community Service Activities",
    credentialIds: [
      ASPN_CREDENTIAL_IDS.COMMUNITY_ENGAGEMENT,
      ASPN_CREDENTIAL_IDS.PROJECT_IMPLEMENTATION,
    ],
  },
];

export const aspnEcosystemRegistry = Object.freeze({
  version: ASPN_ECOSYSTEM_REGISTRY_VERSION,
  relationshipModel: [
    "program",
    "learningExperience",
    "credential",
    "pathway",
    "opportunity",
    "civicRoleFamily",
  ],
  pathways,
  credentials: {
    core: coreCredentials,
    advanced: advancedCredentials,
  },
  programs,
  learningExperiences: [],
  opportunities: [],
});

export function getAspnRegistryCredentials() {
  return [...aspnEcosystemRegistry.credentials.core, ...aspnEcosystemRegistry.credentials.advanced];
}

export function getAspnRegistryCredentialById(credentialId) {
  return getAspnRegistryCredentials().find((credential) => credential.id === credentialId) || null;
}

export function findAspnRegistryCredentialMatch(earnedCredential) {
  const earnedId = normalizeCredentialKey(earnedCredential?.credentialID);
  const earnedName = normalizeCredentialKey(earnedCredential?.credentialName);
  return getAspnRegistryCredentials().find((credential) => {
    const registryId = normalizeCredentialKey(credential.id);
    const registryName = normalizeCredentialKey(credential.name);
    return (earnedId && earnedId === registryId) || (earnedName && earnedName === registryName);
  }) || null;
}

function normalizeCredentialKey(value) {
  return String(value || "")
    .toLowerCase()
    .trim()
    .replace(/credential$/, "")
    .replace(/[^a-z0-9]+/g, "")
    .trim();
}
