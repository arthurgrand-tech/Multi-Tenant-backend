import os
 
# Define the file and folder structure as a dictionary
structure = {
    'pom.xml': None,
    'src': {
        'main': {
            'java': {
                'com': {
                    'ArthurGrand': {
                        'TimesheetApplication.java': None,
                        'admin': {
                            'controller': {
                                'AdminTenantController.java': None
                            },
                            'dto': {
                                'TenantCreationDto.java': None
                            },
                            'service': {
                                'EmailService.java': None
                            }
                        },
                        'common': {
                            'exception': {
                                'GlobalExceptionHandler.java': None
                            }
                        },
                        'config': {
                            'WebSecurityConfig.java': None
                        },
                        'module': {
                            'department': {},
                            'employee': {},
                            'project': {},
                            'timesheet': {}
                        },
                        'multitenancy': {},
                        'security': {}
                    }
                }
            },
            'resources': {
                'application.properties': None,
                'db': {
                    'migration': {
                        'V1__init_master_schema.sql': None,
                        'V2__init_tenant_schema.sql': None
                    }
                }
            },
        },
        'test': {
            'java': {
                'com': {
                    'ArthurGrand': {}
                }
            }
        }
    },
    'db_scripts': {
        'master_database_schema_mysql.sql': None,
        'tenant_database_template_mysql.sql': None
    },
    'postman': {
        'ArthurGrand_Timesheet_API.postman_collection.json': None
    }
}
 
# Function to create directories and files based on the structure
def create_structure(base_path, structure):
    for name, value in structure.items():
        # Create the full path
        full_path = os.path.join(base_path, name)
        if value is None:
            # Create file if value is None
            open(full_path, 'w').close()
        else:
            # Create directory and recurse
            os.makedirs(full_path, exist_ok=True)
            create_structure(full_path, value)
 
# Start the creation process from the current directory
create_structure('.', structure)
 
print("Directory structure created successfully.")
 
 