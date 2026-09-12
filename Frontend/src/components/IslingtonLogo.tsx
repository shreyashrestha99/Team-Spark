import React from 'react'
import { Link } from 'react-router-dom'

interface IslingtonLogoProps {
  className?: string
  showNepali?: boolean
  size?: 'sm' | 'md' | 'lg'
}

export const IslingtonLogo: React.FC<IslingtonLogoProps> = ({
  className = '',
  showNepali = true,
  size = 'md',
}) => {
  const scale = size === 'sm' ? 0.75 : size === 'lg' ? 1.25 : 1

  return (
    <Link to="/" className={`inline-flex items-center gap-3.5 no-underline ${className}`}>
      {/* Islington College Section */}
      <div className="flex flex-col items-start">
        <div className="flex items-center gap-1.5">
          {/* Graduation cap icon */}
          <svg
            width={24 * scale}
            height={24 * scale}
            viewBox="0 0 24 24"
            fill="none"
            className="text-[#D92D20]"
          >
            <path
              d="M12 3L1 9L12 15L21 10.09V17H23V9M5 13.18V17.18C5 19.39 8.13 21.18 12 21.18C15.87 21.18 19 19.39 19 17.18V13.18L12 17L5 13.18Z"
              fill="#D92D20"
            />
          </svg>
          <span
            className="font-bold tracking-tight text-[#D92D20]"
            style={{ fontSize: `${22 * scale}px`, fontFamily: 'Inter, system-ui, sans-serif' }}
          >
            islington <span className="font-semibold text-[#D92D20]">college</span>
          </span>
        </div>

        {showNepali && (
          <span
            className="text-[#475467] font-medium tracking-wide"
            style={{
              fontSize: `${13 * scale}px`,
              marginLeft: `${28 * scale}px`,
              marginTop: '-3px',
            }}
          >
            (इस्लिङ्गटन कलेज)
          </span>
        )}
      </div>

      {/* Divider */}
      <div className="h-7 w-[1.5px] bg-[#D0D5DD]" />

      {/* ING Badge */}
      <div
        className="flex items-center justify-center rounded-[8px] bg-[#43A047] px-2.5 py-1 text-white shadow-sm font-bold tracking-tight"
        style={{ fontSize: `${15 * scale}px` }}
      >
        ing<sup className="text-[9px] ml-0.5 font-normal">®</sup>
      </div>
    </Link>
  )
}
